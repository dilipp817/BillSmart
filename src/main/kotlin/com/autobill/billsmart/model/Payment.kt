package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Payment Entity - Represents a payment transaction
 *
 * Features:
 * - Multiple payment methods (CASH, CARD, UPI, WALLET)
 * - Payment status tracking (PENDING, SUCCESS, FAILED, REFUNDED)
 * - Idempotency through reference number
 * - Transaction ID tracking for external payments
 * - Optimistic locking for concurrency
 */
@Entity
@jakarta.persistence.Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payments_bill_id", columnList = "bill_id"),
        Index(name = "idx_payments_order_id", columnList = "order_id"),
        Index(name = "idx_payments_status", columnList = "status"),
        Index(name = "idx_payments_created_at", columnList = "created_at"),
        Index(name = "idx_payments_reference_number", columnList = "reference_number")
    ]
)
class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    @Suppress("unused")
    var bill: Bill? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null

    @field:NotBlank(message = "Payment method is required")
    @Column(name = "payment_method", nullable = false, length = 50)
    @Suppress("unused")
    var paymentMethod: String = ""

    @field:Positive(message = "Payment amount must be greater than 0")
    @Column(name = "amount", nullable = false)
    @Suppress("unused")
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(name = "status", nullable = false, length = 50)
    var status: String = "PENDING"

    @Column(name = "transaction_id", length = 100)
    @Suppress("unused")
    var transactionId: String? = null

    @field:NotBlank(message = "Reference number is required")
    @Column(name = "reference_number", unique = true, nullable = false, length = 100)
    @Suppress("unused")
    var referenceNumber: String = ""

    @Column(name = "notes", columnDefinition = "TEXT")
    @Suppress("unused")
    var notes: String? = null

    /**
     * Change returned to customer for cash payments.
     * change_amount = amount_tendered - bill_total (only > 0 for CASH overpayments).
     */
    @Column(name = "change_amount", nullable = false)
    var changeAmount: BigDecimal = BigDecimal.ZERO

    @Version
    var version: Long? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    /**
     * Mark payment as successful
     */
    @Suppress("unused")
    fun markAsSuccess() {
        status = "SUCCESS"
        updatedAt = LocalDateTime.now()
    }

    /**
     * Mark payment as failed
     */
    @Suppress("unused")
    fun markAsFailed() {
        status = "FAILED"
        updatedAt = LocalDateTime.now()
    }

    /**
     * Mark payment as refunded
     */
    @Suppress("unused")
    fun markAsRefunded() {
        status = "REFUNDED"
        updatedAt = LocalDateTime.now()
    }

    /**
     * Check if payment can be processed
     */
    @Suppress("unused")
    fun canBeProcessed(): Boolean {
        return status == "PENDING"
    }

    /**
     * Check if payment is successful
     */
    @Suppress("unused")
    fun isSuccessful(): Boolean {
        return status == "SUCCESS"
    }
}

