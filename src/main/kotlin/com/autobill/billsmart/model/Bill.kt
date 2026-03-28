package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Bill Entity - Represents an invoice/bill for an order
 *
 * Features:
 * - Auto-generated unique bill number
 * - GST tax calculation (18% split: 9% CGST + 9% SGST)
 * - Status tracking (ISSUED, PAID, CANCELLED)
 * - Optimistic locking for concurrency
 */
@Entity
@Table(
    name = "bills",
    indexes = [
        Index(name = "idx_bills_order_id", columnList = "order_id"),
        Index(name = "idx_bills_restaurant_id", columnList = "restaurant_id"),
        Index(name = "idx_bills_bill_number", columnList = "bill_number"),
        Index(name = "idx_bills_status", columnList = "status"),
        Index(name = "idx_bills_created_at", columnList = "created_at")
    ]
)
class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @field:NotBlank(message = "Bill number is required")
    @Column(name = "bill_number", unique = true, nullable = false, length = 50)
    @Suppress("unused")
    var billNumber: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: Restaurant? = null

    @Column(name = "subtotal", nullable = false)
    var subtotal: BigDecimal = BigDecimal.ZERO

    @Column(name = "tax_amount", nullable = false)
    var taxAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "cgst_amount", nullable = false)
    @Suppress("unused")
    var cgstAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "sgst_amount", nullable = false)
    @Suppress("unused")
    var sgstAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "discount_amount", nullable = false)
    var discountAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "status", nullable = false, length = 50)
    var status: String = "ISSUED"

    @OneToMany(mappedBy = "bill", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @Suppress("unused")
    var billItems: MutableList<BillItem> = mutableListOf()

    @Version
    var version: Long? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    /**
     * Calculate total amount from subtotal, taxes, and discount
     */
    @Suppress("unused")
    fun calculateTotal() {
        totalAmount = subtotal + taxAmount - discountAmount
    }

    /**
     * Mark bill as paid
     */
    @Suppress("unused")
    fun markAsPaid() {
        status = "PAID"
        updatedAt = LocalDateTime.now()
    }

    /**
     * Cancel bill
     */
    @Suppress("unused")
    fun cancel() {
        status = "CANCELLED"
        updatedAt = LocalDateTime.now()
    }

    /**
     * Check if bill can be modified
     */
    @Suppress("unused")
    fun canBeModified(): Boolean {
        return status == "ISSUED"
    }
}

