package com.autobill.billsmart.model.enums

/**
 * OrderStatus Enum - Represents all possible states of an order
 *
 * States:
 * - PENDING: Order created, waiting to be served
 * - IN_PROGRESS: Order is being prepared
 * - COMPLETED: Order is ready to serve
 * - DELIVERED: Order delivered to customer
 * - CANCELLED: Order cancelled by customer/staff
 * - HOLD: Order placed on hold
 */
enum class OrderStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED,
    CANCELLED,
    HOLD;

    /**
     * Check if order can be modified
     */
    fun canBeModified(): Boolean = this in listOf(PENDING, HOLD)

    /**
     * Check if order can be cancelled
     */
    fun canBeCancelled(): Boolean = this in listOf(PENDING, IN_PROGRESS, HOLD)

    /**
     * Check if order is final (cannot be changed)
     */
    fun isFinal(): Boolean = this in listOf(DELIVERED, CANCELLED)
}

