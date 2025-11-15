package com.autobill.billsmart.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FoodRequest(
    @field:NotBlank
    val name: String,

    @field:NotNull
    val price: Double
)

