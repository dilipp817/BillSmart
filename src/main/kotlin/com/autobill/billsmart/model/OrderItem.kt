package com.autobill.billsmart.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * OrderItem Entity - Represents individual items in an order
 *
 * Business Rules:
 * - Each order item belongs to exactly one order
 * - Each order item references exactly one food item
 * - Quantity must be positive
 * - Unit price is snapshot at order time (for historical accuracy)
 * - Subtotal = quantity * unitPrice
 *
 * Performance: Indexes on order_id for efficient queries
 */
@Entity
@Table(
    name = "order_items",
    indexes = [
        Index(name = "idx_order_items_order", columnList = "order_id"),
        Index(name = "idx_order_items_food", columnList = "food_id")
    ]
)
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    var food: Food? = null

    /**
     * Quantity of the food item
     * Must be > 0
     */
    @Column(nullable = false)
    var quantity: Int = 1

    /**
     * Unit price at time of order (snapshot)
     * Helps maintain historical accuracy
     */
    @Column(nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO

    /**
     * Calculated subtotal (quantity * unitPrice)
     * Denormalized for performance
     */
    @Column(nullable = false, precision = 10, scale = 2)
    var subtotal: BigDecimal = BigDecimal.ZERO

    /**
     * Special requests or modifications for this item
     * e.g., "Extra spicy", "No onions", "Extra cheese"
     */
    @Column(columnDefinition = "TEXT")
    var specialRequests: String? = null

    /**
     * Status of this specific item preparation
     * Can be: PENDING, IN_PROGRESS, READY, SERVED, CANCELLED
     */
    @Column(nullable = false, length = 20)
    var itemStatus: String = "PENDING"

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @Version
    @Column(name = "version")
    var version: Long = 0

    // ==================== BUSINESS LOGIC ====================

    /**
     * Calculate subtotal (quantity * unitPrice)
     */
    fun calculateSubtotal() {
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }
        if (unitPrice < BigDecimal.ZERO) {
            throw IllegalArgumentException("Unit price cannot be negative")
        }
        subtotal = unitPrice.multiply(BigDecimal(quantity))
    }

    /**
     * Update quantity and recalculate subtotal
     */
    fun updateQuantity(newQuantity: Int) {
        if (newQuantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }
        this.quantity = newQuantity
        calculateSubtotal()
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Check if item can be modified
     */
    fun canBeModified(): Boolean = itemStatus in listOf("PENDING", "IN_PROGRESS")

    /**
     * Update item status
     */
    fun updateStatus(newStatus: String) {
        val validStatuses = listOf("PENDING", "IN_PROGRESS", "READY", "SERVED", "CANCELLED")
        if (!validStatuses.contains(newStatus)) {
            throw IllegalArgumentException("Invalid status: $newStatus")
        }
        this.itemStatus = newStatus
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Mark item as served
     */
    @Suppress("unused")
    fun markAsServed() {
        this.itemStatus = "SERVED"
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Cancel this item
     */
    fun cancel() {
        if (!canBeModified()) {
            throw IllegalStateException("Cannot cancel item with status $itemStatus")
        }
        this.itemStatus = "CANCELLED"
        this.updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "OrderItem(id=$id, quantity=$quantity, subtotal=$subtotal, itemStatus='$itemStatus')"
    }
}

