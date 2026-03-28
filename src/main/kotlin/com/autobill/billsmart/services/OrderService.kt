package com.autobill.billsmart.services

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.model.enums.OrderStatus
import java.time.LocalDateTime

/**
 * OrderService Interface - Service contract for order operations
 *
 * SOLID Principles:
 * - Interface Segregation: Focused service contract
 * - Dependency Inversion: Depends on abstraction
 */
interface OrderService {

    // ==================== CREATE ====================
    fun createOrder(restaurantId: Long, request: CreateOrderRequest): OrderResponse
    fun addItemToOrder(orderId: Long, request: AddOrderItemRequest): OrderResponse

    // ==================== READ ====================
    fun getOrder(id: Long): OrderResponse?
    fun getOrdersByRestaurant(restaurantId: Long): List<OrderResponse>
    fun getOrdersByStatus(restaurantId: Long, status: OrderStatus): List<OrderResponse>
    fun getActiveOrders(restaurantId: Long): List<OrderResponse>
    fun getOrderByOrderNumber(orderNumber: String): OrderResponse?
    fun getOrdersByTable(tableId: Long): List<OrderResponse>
    fun getActiveOrderByTable(tableId: Long): OrderResponse?
    fun getOrdersByDateRange(restaurantId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<OrderResponse>
    fun countPendingOrders(restaurantId: Long): Long

    // ==================== UPDATE ====================
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderResponse
    fun updateOrderItem(orderId: Long, itemId: Long, request: UpdateOrderItemRequest): OrderResponse
    fun cancelOrderItem(orderId: Long, itemId: Long): OrderResponse
    fun updateItemStatus(orderId: Long, itemId: Long, newStatus: String): OrderResponse

    // ==================== DELETE ====================
    fun removeItemFromOrder(orderId: Long, itemId: Long): OrderResponse
    fun cancelOrder(orderId: Long): OrderResponse

    // ==================== UTILITY ====================
    fun getOrderSummary(orderId: Long): OrderSummaryResponse?
    fun canModifyOrder(orderId: Long): Boolean
    fun canCancelOrder(orderId: Long): Boolean
}

