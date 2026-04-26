package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.model.enums.OrderStatus
import com.autobill.billsmart.security.TenantUtils
import com.autobill.billsmart.services.BillService
import com.autobill.billsmart.services.OrderService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * OrdersController - REST API endpoints for order operations
 *
 * Base URL: /api/v1/restaurants/{restaurantId}/orders
 *
 * Design Patterns:
 * - RESTful API design
 * - Comprehensive error handling
 * - Standardized response format
 */
@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/orders")
@Validated
class OrdersController(
    private val orderService: OrderService,
    private val billService: BillService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE OPERATIONS ====================

    /**
     * Create a new order
     *
     * POST /api/v1/restaurants/{restaurantId}/orders
     *
     * @param restaurantId the restaurant ID
     * @param request the order creation request
     * @return created order response with 201 status
     */
    @PostMapping
    fun createOrder(
        @PathVariable restaurantId: Long,
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("POST: Create order for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val response = orderService.createOrder(restaurantId, request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(
                    ApiResponse.success(
                        data = response,
                        message = "Order created successfully"
                    )
                )
        } catch (e: AppException) {
            logger.error("Error creating order: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Add item to existing order
     *
     * POST /api/v1/restaurants/{restaurantId}/orders/{orderId}/items
     */
    @PostMapping("/{orderId}/items")
    fun addItemToOrder(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @Valid @RequestBody request: AddOrderItemRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("POST: Add item to order: {}", orderId)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val response = orderService.addItemToOrder(orderId, request)
            ResponseEntity.status(HttpStatus.OK)
                .body(
                    ApiResponse.success(
                        data = response,
                        message = "Item added to order successfully"
                    )
                )
        } catch (e: AppException) {
            logger.error("Error adding item: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Get all orders for restaurant
     *
     * GET /api/v1/restaurants/{restaurantId}/orders
     */
    @GetMapping
    fun getOrders(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<OrderListResponse>> {
        logger.info("GET: Fetch all orders for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val orders = orderService.getOrdersByRestaurant(restaurantId)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = OrderListResponse(
                        orders = orders,
                        total = orders.size.toLong(),
                        status = "success"
                    ),
                    message = "Orders retrieved successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error fetching orders: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Get order by ID
     *
     * GET /api/v1/restaurants/{restaurantId}/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    fun getOrder(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("GET: Fetch order - ID: {}", orderId)
        TenantUtils.assertTenantAccess(restaurantId)

        val order = orderService.getOrder(orderId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", "Order not found"))

        // Tenant isolation: return 404 rather than 403 to avoid leaking cross-tenant IDs
        if (order.restaurantId != restaurantId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", "Order not found"))
        }

        return ResponseEntity.ok(
            ApiResponse.success(
                data = order,
                message = "Order retrieved successfully"
            )
        )
    }

    /**
     * Get orders by status
     *
     * GET /api/v1/restaurants/{restaurantId}/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    fun getOrdersByStatus(
        @PathVariable restaurantId: Long,
        @PathVariable status: OrderStatus
    ): ResponseEntity<ApiResponse<OrderListResponse>> {
        logger.info("GET: Fetch orders by status - status: {}", status)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val orders = orderService.getOrdersByStatus(restaurantId, status)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = OrderListResponse(
                        orders = orders,
                        total = orders.size.toLong(),
                        status = "success"
                    ),
                    message = "Orders retrieved successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error fetching orders: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Get active orders (not completed/cancelled)
     *
     * GET /api/v1/restaurants/{restaurantId}/orders/active
     */
    @GetMapping("/active")
    fun getActiveOrders(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<OrderListResponse>> {
        logger.info("GET: Fetch active orders for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        val orders = orderService.getActiveOrders(restaurantId)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = OrderListResponse(
                    orders = orders,
                    total = orders.size.toLong(),
                    status = "success"
                ),
                message = "Active orders retrieved successfully"
            )
        )
    }

    /**
     * Get orders by date range
     *
     * GET /api/v1/restaurants/{restaurantId}/orders/range
     */
    @GetMapping("/range")
    fun getOrdersByDateRange(
        @PathVariable restaurantId: Long,
        @RequestParam(name = "start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam(name = "end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ApiResponse<OrderListResponse>> {
        logger.info("GET: Fetch orders by date range - start: {}, end: {}", startDate, endDate)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val orders = orderService.getOrdersByDateRange(restaurantId, startDate, endDate)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = OrderListResponse(
                        orders = orders,
                        total = orders.size.toLong(),
                        status = "success"
                    ),
                    message = "Orders retrieved successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error fetching orders: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Get pending orders count
     *
     * GET /api/v1/restaurants/{restaurantId}/orders/count/pending
     */
    @GetMapping("/count/pending")
    fun countPendingOrders(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<Map<String, Long>>> {
        logger.info("GET: Count pending orders for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        val count = orderService.countPendingOrders(restaurantId)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = mapOf("pending_count" to count),
                message = "Pending orders count retrieved"
            )
        )
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update order status
     *
     * PATCH /api/v1/restaurants/{restaurantId}/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @Valid @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("PATCH: Update order status - ID: {}, newStatus: {}", orderId, request.status)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val response = orderService.updateOrderStatus(orderId, request.status)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = response,
                    message = "Order status updated successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error updating order status: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Update order item quantity
     *
     * PUT /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}
     */
    @PutMapping("/{orderId}/items/{itemId}")
    fun updateOrderItem(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @PathVariable @Positive itemId: Long,
        @Valid @RequestBody request: UpdateOrderItemRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("PUT: Update order item - orderId: {}, itemId: {}", orderId, itemId)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val response = orderService.updateOrderItem(orderId, itemId, request)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = response,
                    message = "Order item updated successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error updating order item: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Update individual item status
     *
     * PATCH /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status
     */
    @PatchMapping("/{orderId}/items/{itemId}/status")
    fun updateItemStatus(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @PathVariable @Positive itemId: Long,
        @RequestParam(name = "new_status") newStatus: String
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("PATCH: Update item status - orderId: {}, itemId: {}, status: {}", orderId, itemId, newStatus)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val response = orderService.updateItemStatus(orderId, itemId, newStatus)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = response,
                    message = "Item status updated successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error updating item status: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Remove item from order
     *
     * DELETE /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}
     */
    @DeleteMapping("/{orderId}/items/{itemId}")
    fun removeItemFromOrder(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @PathVariable @Positive itemId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("DELETE: Remove item from order - orderId: {}, itemId: {}", orderId, itemId)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val response = orderService.removeItemFromOrder(orderId, itemId)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = response,
                    message = "Item removed from order successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error removing item: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Cancel order
     *
     * DELETE /api/v1/restaurants/{restaurantId}/orders/{orderId}
     * Restricted to MANAGER and ADMIN roles (enforced in SecurityConfig).
     */
    @DeleteMapping("/{orderId}")
    fun cancelOrder(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("DELETE: Cancel order - ID: {}", orderId)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val response = orderService.cancelOrder(orderId)
            ResponseEntity.ok(
                ApiResponse.success(
                    data = response,
                    message = "Order cancelled successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error cancelling order: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== EXCEPTION HANDLING ====================

    /**
     * Auto-generate a bill for an order — no manual calculation needed.
     * Backend computes subtotal from order items, applies 18% GST (9% CGST + 9% SGST).
     * Bill number is auto-generated as BILL-{restaurantId}-{yyyyMMdd}-{seq}.
     *
     * POST /api/v1/restaurants/{restaurantId}/orders/{orderId}/generate-bill
     */
    @PostMapping("/{orderId}/generate-bill")
    fun generateBill(
        @PathVariable restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @RequestParam(required = false, defaultValue = "0") discount: BigDecimal
    ): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("POST: Generate bill for order: {}, discount: {}", orderId, discount)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            requireOrderBelongsToRestaurant(restaurantId, orderId)
            val bill = billService.generateBillForOrder(orderId, discount)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bill, "Bill generated successfully"))
        } catch (e: AppException) {
            logger.error("Error generating bill: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Search orders by order number, table number, or status keyword.
     *
     * GET /api/v1/restaurants/{restaurantId}/orders/search?q=ORD-20260404
     */
    @GetMapping("/search")
    fun searchOrders(
        @PathVariable restaurantId: Long,
        @RequestParam q: String
    ): ResponseEntity<ApiResponse<OrderListResponse>> {
        logger.info("GET: Search orders - restaurant: {}, q: {}", restaurantId, q)

        return try {
            TenantUtils.assertTenantAccess(restaurantId)
            val allOrders = orderService.getOrdersByRestaurant(restaurantId)
            val matched = allOrders.filter { order ->
                order.orderNumber.contains(q, ignoreCase = true) ||
                order.tableNumber?.contains(q, ignoreCase = true) == true ||
                order.status.name.contains(q, ignoreCase = true)
            }
            ResponseEntity.ok(
                ApiResponse.success(
                    OrderListResponse(orders = matched, total = matched.size.toLong()),
                    "Search completed successfully"
                )
            )
        } catch (e: AppException) {
            logger.error("Error searching orders: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== HELPERS ====================

    /**
     * Tenant isolation guard — ensures the order belongs to the restaurant in the URL path.
     * Returns 404 (not 403) to avoid revealing that the order exists in a different tenant.
     * Must be called before any mutation on an order-scoped endpoint.
     */
    private fun requireOrderBelongsToRestaurant(restaurantId: Long, orderId: Long) {
        val order = orderService.getOrder(orderId)
            ?: throw AppException.ResourceNotFoundException("Order not found")
        if (order.restaurantId != restaurantId)
            throw AppException.ResourceNotFoundException("Order not found")
    }

    /**
     * Handle AppException and convert to appropriate HTTP response
     */
    private fun <T> handleAppException(e: AppException): ResponseEntity<ApiResponse<T>> {
        return when (e) {
            is AppException.ResourceNotFoundException -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.message ?: "Resource not found"))
            is AppException.ValidationException -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", e.message ?: "Validation failed"))
            is AppException.ConflictException -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", e.message ?: "Conflict occurred"))
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Internal server error"))
        }
    }
}

