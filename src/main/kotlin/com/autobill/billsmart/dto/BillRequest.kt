package com.autobill.billsmart.dto

import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * BillRequest - DTO for creating/updating bills
 */
data class BillRequest(
    /** Optional — if not provided, server auto-generates using pattern BILL-{restaurantId}-{yyyyMMdd}-{seq} */
    val billNumber: String? = null,

    @field:Positive(message = "Order ID must be positive")
    val orderId: Long,

    @field:Positive(message = "Restaurant ID must be positive")
    val restaurantId: Long,

    val subtotal: BigDecimal,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val cgstAmount: BigDecimal = BigDecimal.ZERO,
    val sgstAmount: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,

    val status: String? = "ISSUED"
)

/**
 * BillResponse - DTO for bill responses
 */
data class BillResponse(
    val id: Long,
    val billNumber: String,
    val orderId: Long,
    val restaurantId: Long,
    val restaurantName: String? = null,
    val subtotal: BigDecimal,
    val taxAmount: BigDecimal,
    val cgstAmount: BigDecimal,
    val sgstAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val status: String,
    val billItems: List<BillItemResponse> = emptyList(),
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * BillListResponse - Lightweight bill for list endpoints
 */
data class BillListResponse(
    val id: Long,
    val billNumber: String,
    val orderId: Long,
    val restaurantName: String? = null,
    val totalAmount: BigDecimal,
    val status: String,
    val createdAt: LocalDateTime? = null
)

