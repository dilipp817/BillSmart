package com.autobill.billsmart.dto

import com.autobill.billsmart.model.enums.OrderStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * CreateOrderRequest - Request to create a new order
 */
data class CreateOrderRequest(
    /** Nullable — null for TAKEAWAY orders (no table). When provided, must be positive. DINE_IN requiredness is enforced in the service layer. */
    @field:Positive(message = "Table ID must be positive")
    val tableId: Long? = null,

    @field:NotEmpty(message = "Order must have at least one item")
    @field:Valid
    val items: List<OrderItemRequest>,

    /** DINE_IN = at table, TAKEAWAY = customer collects, DELIVERY = we deliver. Defaults to DINE_IN. */
    val orderType: String = "DINE_IN",

    val notes: String? = null
)

/**
 * OrderItemRequest - Item details for order
 */
data class OrderItemRequest(
    @field:NotNull(message = "Food ID is required")
    @field:Positive(message = "Food ID must be positive")
    val foodId: Long,

    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be greater than 0")
    val quantity: Int,

    val specialRequests: String? = null
)

/**
 * UpdateOrderItemRequest - Request to update order item quantity
 */
data class UpdateOrderItemRequest(
    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be greater than 0")
    val quantity: Int,

    val specialRequests: String? = null
)

/**
 * OrderStatusUpdateRequest - Request to update order status
 */
data class OrderStatusUpdateRequest(
    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)

/**
 * OrderResponse - Full order details response
 */
data class OrderResponse(
    val id: Long,
    val restaurantId: Long,
    val tableId: Long?,
    val tableNumber: String?,
    val orderNumber: String,
    val status: OrderStatus,
    val orderType: String,
    val items: List<OrderItemResponse>,
    /** Pre-tax sum of all item subtotals. Same value as total_amount at the order stage. */
    val subtotal: BigDecimal,
    val totalAmount: BigDecimal,
    val notes: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val version: Long = 0
)

/**
 * OrderItemResponse - Order item details
 */
data class OrderItemResponse(
    val id: Long,
    val foodId: Long,
    val foodName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val itemStatus: String,
    val specialRequests: String? = null,
    val createdAt: LocalDateTime
)

/**
 * OrderListResponse - Response for order list operations
 */
data class OrderListResponse(
    val orders: List<OrderResponse>,
    val total: Long,
    val status: String = "success"
)

/**
 * OrderSummaryResponse - Summary for quick views
 */
data class OrderSummaryResponse(
    val id: Long,
    val orderNumber: String,
    val tableNumber: String?,
    val status: OrderStatus,
    val itemCount: Int,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime
)

/**
 * AddOrderItemRequest - Request to add item to existing order
 */
data class AddOrderItemRequest(
    @field:NotNull(message = "Food ID is required")
    @field:Positive(message = "Food ID must be positive")
    val foodId: Long,

    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be greater than 0")
    val quantity: Int,

    val specialRequests: String? = null
)

