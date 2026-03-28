package com.autobill.billsmart.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * BillItemRequest - DTO for bill item requests
 */
data class BillItemRequest(
    @field:Positive(message = "Food ID must be positive")
    val foodId: Long,

    @field:Positive(message = "Quantity must be greater than 0")
    val quantity: Int,

    val unitPrice: BigDecimal
)

/**
 * BillItemResponse - DTO for bill item responses
 */
data class BillItemResponse(
    val id: Long,
    val billId: Long,
    val foodId: Long,
    val foodName: String? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val itemTotal: BigDecimal,
    val createdAt: LocalDateTime? = null
)

