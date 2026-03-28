package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.model.enums.OrderStatus
import com.autobill.billsmart.services.OrderService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
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
    private val orderService: OrderService
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
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @Valid @RequestBody request: AddOrderItemRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("POST: Add item to order: {}", orderId)

        return try {
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
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("GET: Fetch order - ID: {}", orderId)

        val order = orderService.getOrder(orderId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                    ApiResponse.error(
                        code = "NOT_FOUND",
                        message = "Order not found"
                    )
                )

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
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ApiResponse<OrderListResponse>> {
        logger.info("GET: Fetch orders by date range - start: {}, end: {}", startDate, endDate)

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
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @Valid @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("PATCH: Update order status - ID: {}, newStatus: {}", orderId, request.status)

        return try {
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
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @PathVariable @Positive itemId: Long,
        @Valid @RequestBody request: UpdateOrderItemRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("PUT: Update order item - orderId: {}, itemId: {}", orderId, itemId)

        return try {
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
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @PathVariable @Positive itemId: Long,
        @RequestParam newStatus: String
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("PATCH: Update item status - orderId: {}, itemId: {}, status: {}", orderId, itemId, newStatus)

        return try {
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
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long,
        @PathVariable @Positive itemId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("DELETE: Remove item from order - orderId: {}, itemId: {}", orderId, itemId)

        return try {
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
     */
    @DeleteMapping("/{orderId}")
    fun cancelOrder(
        @PathVariable _restaurantId: Long,
        @PathVariable @Positive orderId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        logger.info("DELETE: Cancel order - ID: {}", orderId)

        return try {
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

