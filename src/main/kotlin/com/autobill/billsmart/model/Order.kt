package com.autobill.billsmart.model

import com.autobill.billsmart.model.enums.OrderStatus
import com.autobill.billsmart.model.enums.OrderType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Order Entity - Represents a restaurant order
 *
 * Business Rules:
 * - Each order belongs to exactly one restaurant
 * - Each order is associated with one table
 * - Order contains multiple order items (1:N relationship)
 * - Order can have one bill (1:1 relationship)
 * - Order total is calculated from items
 *
 * Thread Safety: Handled via @Version optimistic locking
 * Performance: Indexes on restaurant_id, status, and created_at
 */
@Entity
@jakarta.persistence.Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_restaurant", columnList = "restaurant_id"),
        Index(name = "idx_orders_status", columnList = "status"),
        Index(name = "idx_orders_table", columnList = "table_id"),
        Index(name = "idx_orders_created_at", columnList = "created_at")
    ]
)
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: Restaurant? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "table_id", nullable = true)
    var table: Table? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PENDING

    /**
     * Order type: DINE_IN (at table), TAKEAWAY (customer collects), DELIVERY (we deliver)
     */
    @Column(name = "order_type", nullable = false, length = 20)
    var orderType: String = "DINE_IN"

    /**
     * Order number for identification (e.g., "ORD-2026-03-28-001")
     */
    @Column(nullable = false, unique = true, length = 50)
    var orderNumber: String = ""

    /**
     * Total amount for the order
     * Calculated from sum of order items
     */
    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    /**
     * Order items (1:N relationship)
     */
    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var items: MutableList<OrderItem> = mutableListOf()


    /**
     * Special instructions or notes for the order
     */
    @Column(columnDefinition = "TEXT")
    var notes: String? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    /**
     * Version field for optimistic locking
     */
    @Version
    @Column(name = "version")
    var version: Long = 0

    // ==================== BUSINESS LOGIC ====================

    /**
     * Check if order can be modified (add/remove items)
     */
    fun canBeModified(): Boolean = status.canBeModified()

    /**
     * Check if order can be cancelled
     */
    fun canBeCancelled(): Boolean = status.canBeCancelled()

    /**
     * Add item to order
     * Recalculates total amount
     */
    fun addItem(item: OrderItem) {
        if (!canBeModified()) {
            throw IllegalStateException("Cannot add items to order with status $status")
        }
        item.order = this
        items.add(item)
        recalculateTotal()
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Remove item from order
     * Recalculates total amount
     */
    fun removeItem(item: OrderItem) {
        if (!canBeModified()) {
            throw IllegalStateException("Cannot remove items from order with status $status")
        }
        items.remove(item)
        recalculateTotal()
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Recalculate total amount from all items
     * Should be called after any item change
     */
    fun recalculateTotal() {
        totalAmount = items.sumOf { it.subtotal }
    }

    /**
     * Update order status with validation
     */
    fun updateStatus(newStatus: OrderStatus) {
        val validTransitions: List<OrderStatus> = when (status) {
            OrderStatus.PENDING -> listOf(OrderStatus.IN_PROGRESS, OrderStatus.HOLD, OrderStatus.CANCELLED)
            OrderStatus.IN_PROGRESS -> listOf(OrderStatus.COMPLETED, OrderStatus.HOLD, OrderStatus.CANCELLED)
            OrderStatus.COMPLETED -> listOf(OrderStatus.DELIVERED)
            OrderStatus.DELIVERED -> emptyList() // Final state
            OrderStatus.CANCELLED -> emptyList() // Final state
            OrderStatus.HOLD -> listOf(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED)
        }

        if (!validTransitions.contains(newStatus)) {
            throw IllegalArgumentException(
                "Cannot transition from $status to $newStatus"
            )
        }

        this.status = newStatus
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Get number of items in order
     */
    fun getItemCount(): Int = items.size

    /**
     * Check if order is complete (all items prepared)
     */
    fun isComplete(): Boolean = status == OrderStatus.COMPLETED

    /**
     * Cancel order if possible
     */
    fun cancel() {
        if (!canBeCancelled()) {
            throw IllegalStateException("Order cannot be cancelled with status $status")
        }
        this.status = OrderStatus.CANCELLED
        this.updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Order(id=$id, orderNumber='$orderNumber', status=$status, totalAmount=$totalAmount)"
    }
}

