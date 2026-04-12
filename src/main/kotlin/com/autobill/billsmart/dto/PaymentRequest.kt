package com.autobill.billsmart.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * PaymentRequest - DTO for creating payments
 */
data class PaymentRequest(
    @field:Positive(message = "Bill ID must be positive")
    val billId: Long? = null,

    @field:Positive(message = "Order ID must be positive")
    val orderId: Long,

    @field:NotBlank(message = "Payment method is required")
    val paymentMethod: String, // CASH, CARD, UPI, WALLET

    @field:Positive(message = "Payment amount must be greater than 0")
    val amount: BigDecimal,

    @field:NotBlank(message = "Reference number is required")
    val referenceNumber: String,

    val transactionId: String? = null,
    val notes: String? = null,

    /**
     * Change returned to the customer for CASH payments.
     * Set when the customer pays more than the bill total.
     * Example: bill = ₹450, customer gives ₹500 → change_amount = 50.
     * For non-cash payments, omit or send 0.
     */
    val changeAmount: BigDecimal = BigDecimal.ZERO,

    /**
     * If true, the payment is immediately marked SUCCESS in the same transaction.
     * Use this for CASH, UPI, and WALLET payments where success is known at call time.
     * This collapses the two-step flow (POST → PATCH /process) into a single atomic call,
     * eliminating the orphaned-PENDING-payment risk on network failure.
     *
     * Leave false (default) for CARD payments where you must wait for gateway confirmation.
     */
    val autoProcess: Boolean = false
)

/**
 * PaymentResponse - DTO for payment responses
 */
data class PaymentResponse(
    val id: Long,
    val billId: Long? = null,
    val orderId: Long,
    val paymentMethod: String,
    val amount: BigDecimal,
    val status: String,
    val transactionId: String? = null,
    val referenceNumber: String,
    val notes: String? = null,
    /** Change returned to customer. > 0 only for CASH overpayments. */
    val changeAmount: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * PaymentStatusUpdateRequest - DTO for updating payment status
 */
data class PaymentStatusUpdateRequest(
    @field:NotBlank(message = "Status is required")
    val status: String, // SUCCESS, FAILED, REFUNDED

    val transactionId: String? = null,
    val notes: String? = null
)

/**
 * PaymentListResponse - Lightweight payment for list endpoints
 */
data class PaymentListResponse(
    val id: Long,
    val orderId: Long,
    val paymentMethod: String,
    val amount: BigDecimal,
    val status: String,
    val referenceNumber: String,
    val createdAt: LocalDateTime? = null
)

