package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.persistence.Table
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * BillItem Entity - Represents a line item in a bill
 *
 * Features:
 * - Links food item to bill
 * - Stores quantity and unit price
 * - Calculates item total
 * - Optimistic locking for concurrency
 */
@Entity
@Table(
    name = "bill_items",
    indexes = [
        Index(name = "idx_bill_items_bill_id", columnList = "bill_id"),
        Index(name = "idx_bill_items_food_id", columnList = "food_id")
    ]
)
class BillItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    @Suppress("unused")
    var bill: Bill? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    var food: Food? = null

    @field:Positive(message = "Quantity must be greater than 0")
    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0

    @Column(name = "unit_price", nullable = false)
    var unitPrice: BigDecimal = BigDecimal.ZERO

    @Column(name = "item_total", nullable = false)
    var itemTotal: BigDecimal = BigDecimal.ZERO

    @Version
    var version: Long? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    /**
     * Calculate item total (quantity × unit price)
     */
    @Suppress("unused")
    fun calculateTotal() {
        itemTotal = unitPrice * BigDecimal(quantity)
    }
}

