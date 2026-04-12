package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.mappers.OrderMapper
import com.autobill.billsmart.mappers.createOrderItemFromRequest
import com.autobill.billsmart.model.Order
import com.autobill.billsmart.model.enums.OrderStatus
import com.autobill.billsmart.repositories.*
import com.autobill.billsmart.services.OrderService
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * OrderServiceImpl - Implementation of OrderService
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles order operations
 * - Dependency Inversion: Depends on abstractions
 *
 * Thread Safety:
 * - @Transactional ensures database consistency
 * - @Version handles optimistic locking
 */
@Service
@Transactional
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val restaurantRepository: RestaurantRepository,
    private val tableRepository: TableRepository,
    private val foodRepository: FoodRepository,
    private val orderMapper: OrderMapper
) : OrderService {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE OPERATIONS ====================

    override fun createOrder(restaurantId: Long, request: CreateOrderRequest): OrderResponse {
        logger.debug("Creating order for restaurant: {}", restaurantId)

        // Validate restaurant exists
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow {
                logger.error("Restaurant not found: {}", restaurantId)
                AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
            }

        // Validate table exists and is available
        val table = tableRepository.findById(request.tableId)
            .orElseThrow {
                logger.error("Table not found: {}", request.tableId)
                AppException.ResourceNotFoundException("Table not found: ${request.tableId}")
            }

        if (!table.canAcceptOrder()) {
            logger.warn("Table {} cannot accept order - status: {}", request.tableId, table.status)
            throw AppException.ValidationException("Table is not available for new orders")
        }

        // Create order
        val order = Order()
        order.restaurant = restaurant
        order.table = table
        order.orderNumber = generateOrderNumber()
        order.orderType = request.orderType.uppercase().let {
            if (it in listOf("DINE_IN", "TAKEAWAY", "DELIVERY")) it else "DINE_IN"
        }
        order.notes = request.notes

        // Add items
        request.items.forEach { itemRequest ->
            val food = foodRepository.findById(itemRequest.foodId)
                .orElseThrow {
                    logger.error("Food not found: {}", itemRequest.foodId)
                    AppException.ResourceNotFoundException("Food not found: ${itemRequest.foodId}")
                }

            val orderItem = createOrderItemFromRequest(itemRequest, food, order)
            order.items.add(orderItem)
        }

        // Calculate total
        order.recalculateTotal()

        // Mark table as occupied
        table.occupyWithOrder(order)

        val saved = orderRepository.save(order)
        tableRepository.save(table)

        logger.info("Order created successfully: ID={}, orderNumber={}", saved.id, saved.orderNumber)
        return orderMapper.toResponse(saved)
    }

    override fun addItemToOrder(orderId: Long, request: AddOrderItemRequest): OrderResponse {
        logger.debug("Adding item to order: {}", orderId)

        val order = orderRepository.findById(orderId)
            .orElseThrow {
                logger.error("Order not found: {}", orderId)
                AppException.ResourceNotFoundException("Order not found: $orderId")
            }

        if (!order.canBeModified()) {
            logger.warn("Order {} cannot be modified - status: {}", orderId, order.status)
            throw AppException.ValidationException(
                "Cannot add items to order with status ${order.status}"
            )
        }

        val food = foodRepository.findById(request.foodId)
            .orElseThrow {
                logger.error("Food not found: {}", request.foodId)
                AppException.ResourceNotFoundException("Food not found: ${request.foodId}")
            }

        // Convert AddOrderItemRequest to OrderItemRequest
        val itemRequest = OrderItemRequest(
            foodId = request.foodId,
            quantity = request.quantity,
            specialRequests = request.specialRequests
        )
        val orderItem = createOrderItemFromRequest(itemRequest, food, order)
        order.addItem(orderItem)

        val updated = orderRepository.save(order)
        logger.info("Item added to order: {}", orderId)
        return orderMapper.toResponse(updated)
    }

    // ==================== READ OPERATIONS ====================

    override fun getOrder(id: Long): OrderResponse? {
        logger.debug("Fetching order: {}", id)
        return orderRepository.findById(id)
            .map { orderMapper.toResponse(it) }
            .orElse(null)
    }

    override fun getOrdersByRestaurant(restaurantId: Long): List<OrderResponse> {
        logger.debug("Fetching orders for restaurant: {}", restaurantId)

        if (!restaurantRepository.existsById(restaurantId)) {
            throw AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
        }

        val orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
        logger.debug("Found {} orders for restaurant: {}", orders.size, restaurantId)
        return orderMapper.toResponses(orders)
    }

    override fun getOrdersByStatus(restaurantId: Long, status: OrderStatus): List<OrderResponse> {
        logger.debug("Fetching orders by status - restaurant: {}, status: {}", restaurantId, status)
        val orders = orderRepository.findByRestaurantIdAndStatus(restaurantId, status)
        logger.debug("Found {} orders with status: {}", orders.size, status)
        return orderMapper.toResponses(orders)
    }

    override fun getActiveOrders(restaurantId: Long): List<OrderResponse> {
        logger.debug("Fetching active orders for restaurant: {}", restaurantId)
        val orders = orderRepository.findActiveOrdersByRestaurantId(restaurantId)
        logger.debug("Found {} active orders", orders.size)
        return orderMapper.toResponses(orders)
    }

    override fun getOrderByOrderNumber(orderNumber: String): OrderResponse? {
        logger.debug("Fetching order by number: {}", orderNumber)
        return orderRepository.findByOrderNumber(orderNumber)
            ?.let { orderMapper.toResponse(it) }
    }

    override fun getOrdersByTable(tableId: Long): List<OrderResponse> {
        logger.debug("Fetching orders for table: {}", tableId)
        val orders = orderRepository.findByTableIdOrderByCreatedAtDesc(tableId)
        return orderMapper.toResponses(orders)
    }

    override fun getActiveOrderByTable(tableId: Long): OrderResponse? {
        logger.debug("Fetching active order for table: {}", tableId)
        return orderRepository.findActiveOrderByTableId(tableId)
            ?.let { orderMapper.toResponse(it) }
    }

    override fun getOrdersByDateRange(
        restaurantId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<OrderResponse> {
        logger.debug("Fetching orders by date range - restaurant: {}, start: {}, end: {}",
            restaurantId, startDate, endDate)
        val orders = orderRepository.findByRestaurantIdAndDateRange(restaurantId, startDate, endDate)
        return orderMapper.toResponses(orders)
    }

    override fun countPendingOrders(restaurantId: Long): Long {
        return orderRepository.countPendingOrders(restaurantId)
    }

    // ==================== UPDATE OPERATIONS ====================

    override fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse {
        logger.debug("Updating order status - ID: {}, newStatus: {}", orderId, newStatus)

        try {
            val order = orderRepository.findById(orderId)
                .orElseThrow {
                    logger.error("Order not found: {}", orderId)
                    AppException.ResourceNotFoundException("Order not found: $orderId")
                }

            order.updateStatus(newStatus)
            order.updatedAt = LocalDateTime.now()

            val updated = orderRepository.save(order)
            logger.info("Order status updated - ID: {}, newStatus: {}", orderId, newStatus)
            return orderMapper.toResponse(updated)

        } catch (_: OptimisticLockingFailureException) {
            logger.warn("Concurrent modification detected for order: {}", orderId)
            throw AppException.ConflictException(
                "Order was modified by another user. Please refresh and try again."
            )
        }
    }

    override fun updateOrderItem(orderId: Long, itemId: Long, request: UpdateOrderItemRequest): OrderResponse {
        logger.debug("Updating order item - orderId: {}, itemId: {}", orderId, itemId)

        val order = orderRepository.findById(orderId)
            .orElseThrow {
                AppException.ResourceNotFoundException("Order not found: $orderId")
            }

        val item = orderItemRepository.findById(itemId)
            .orElseThrow {
                AppException.ResourceNotFoundException("Order item not found: $itemId")
            }

        if (item.order?.id != orderId) {
            throw AppException.ValidationException("Item does not belong to this order")
        }

        if (!item.canBeModified()) {
            throw AppException.ValidationException("Item cannot be modified with status ${item.itemStatus}")
        }

        item.updateQuantity(request.quantity)
        request.specialRequests?.let { item.specialRequests = it }

        orderItemRepository.save(item)
        order.recalculateTotal()

        val updated = orderRepository.save(order)
        logger.info("Order item updated - orderId: {}, itemId: {}", orderId, itemId)
        return orderMapper.toResponse(updated)
    }

    override fun cancelOrderItem(orderId: Long, itemId: Long): OrderResponse {
        logger.debug("Cancelling order item - orderId: {}, itemId: {}", orderId, itemId)

        val order = orderRepository.findById(orderId)
            .orElseThrow { AppException.ResourceNotFoundException("Order not found: $orderId") }

        val item = orderItemRepository.findById(itemId)
            .orElseThrow { AppException.ResourceNotFoundException("Order item not found: $itemId") }

        if (item.order?.id != orderId) {
            throw AppException.ValidationException("Item does not belong to this order")
        }

        item.cancel()
        orderItemRepository.save(item)
        order.recalculateTotal()

        val updated = orderRepository.save(order)
        logger.info("Order item cancelled - orderId: {}, itemId: {}", orderId, itemId)
        return orderMapper.toResponse(updated)
    }

    override fun updateItemStatus(orderId: Long, itemId: Long, newStatus: String): OrderResponse {
        logger.debug("Updating item status - orderId: {}, itemId: {}, newStatus: {}", orderId, itemId, newStatus)

        val order = orderRepository.findById(orderId)
            .orElseThrow { AppException.ResourceNotFoundException("Order not found: $orderId") }

        val item = orderItemRepository.findById(itemId)
            .orElseThrow { AppException.ResourceNotFoundException("Order item not found: $itemId") }

        if (item.order?.id != orderId) {
            throw AppException.ValidationException("Item does not belong to this order")
        }

        item.updateStatus(newStatus)
        orderItemRepository.save(item)

        // Auto-complete the order if all items have reached a final state.
        // This removes the dependency on kitchen staff manually updating order status via KDS.
        autoCompleteOrderIfAllItemsFinal(order)

        val updated = orderRepository.save(order)
        logger.info("Item status updated - itemId: {}, newStatus: {}", itemId, newStatus)
        return orderMapper.toResponse(updated)
    }

    // ==================== DELETE OPERATIONS ====================

    override fun removeItemFromOrder(orderId: Long, itemId: Long): OrderResponse {
        logger.debug("Removing item from order - orderId: {}, itemId: {}", orderId, itemId)

        val order = orderRepository.findById(orderId)
            .orElseThrow { AppException.ResourceNotFoundException("Order not found: $orderId") }

        if (!order.canBeModified()) {
            throw AppException.ValidationException("Cannot remove items from order with status ${order.status}")
        }

        val item = orderItemRepository.findById(itemId)
            .orElseThrow { AppException.ResourceNotFoundException("Order item not found: $itemId") }

        order.removeItem(item)
        orderItemRepository.delete(item)

        val updated = orderRepository.save(order)
        logger.info("Item removed from order - orderId: {}", orderId)
        return orderMapper.toResponse(updated)
    }

    override fun cancelOrder(orderId: Long): OrderResponse {
        logger.debug("Cancelling order: {}", orderId)

        try {
            val order = orderRepository.findById(orderId)
                .orElseThrow { AppException.ResourceNotFoundException("Order not found: $orderId") }

            if (!order.canBeCancelled()) {
                throw AppException.ValidationException("Order cannot be cancelled with status ${order.status}")
            }

            order.cancel()

            // Release table if order is cancelled
            order.table?.release()
            order.table?.let { tableRepository.save(it) }

            val updated = orderRepository.save(order)
            logger.info("Order cancelled: {}", orderId)
            return orderMapper.toResponse(updated)

        } catch (_: OptimisticLockingFailureException) {
            logger.warn("Concurrent modification detected for order: {}", orderId)
            throw AppException.ConflictException("Order was modified by another user. Please try again.")
        }
    }

    // ==================== UTILITY OPERATIONS ====================

    override fun getOrderSummary(orderId: Long): OrderSummaryResponse? {
        logger.debug("Fetching order summary: {}", orderId)
        return orderRepository.findById(orderId)
            .map { orderMapper.toSummaryResponse(it) }
            .orElse(null)
    }

    override fun canModifyOrder(orderId: Long): Boolean {
        return orderRepository.findById(orderId)
            .map { it.canBeModified() }
            .orElseGet { false }
    }

    override fun canCancelOrder(orderId: Long): Boolean {
        return orderRepository.findById(orderId)
            .map { it.canBeCancelled() }
            .orElseGet { false }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Auto-complete order when all items reach a final item_status.
     *
     * Trigger: every call to updateItemStatus().
     * Condition: ALL items are in {READY, SERVED, CANCELLED}
     *            AND at least one item is READY or SERVED (not every item cancelled).
     * Action: order.status → COMPLETED (bypasses transition guard — this is server-managed).
     *
     * This removes the operational dependency on kitchen staff manually pressing
     * "Mark Order Complete" on the KDS after all items are done.
     */
    private fun autoCompleteOrderIfAllItemsFinal(order: Order) {
        val items = order.items
        if (items.isEmpty()) return

        val finalStatuses = setOf("READY", "SERVED", "CANCELLED")
        val allFinal = items.all { it.itemStatus in finalStatuses }
        val hasActiveItem = items.any { it.itemStatus in setOf("READY", "SERVED") }
        val alreadyFinal = order.status in listOf(
            OrderStatus.COMPLETED, OrderStatus.DELIVERED, OrderStatus.CANCELLED
        )

        if (allFinal && hasActiveItem && !alreadyFinal) {
            order.status = OrderStatus.COMPLETED
            order.updatedAt = LocalDateTime.now()
            logger.info(
                "Order {} auto-completed — all {} items are in a final state",
                order.id, items.size
            )
        }
    }

    /**
     * Generate unique order number
     * Format: ORD-YYYYMMDD-XXXXX
     */
    private fun generateOrderNumber(): String {
        val date = LocalDateTime.now()
        val random = Random().nextInt(99999)
        return String.format(
            "ORD-%04d%02d%02d-%05d",
            date.year,
            date.monthValue,
            date.dayOfMonth,
            random
        )
    }
}

