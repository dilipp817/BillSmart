package com.autobill.billsmart.dto

import jakarta.validation.constraints.*

data class FoodRequest(
    @field:NotBlank(message = "Food name is required")
    @field:Size(min = 1, max = 255, message = "Food name must be between 1 and 255 characters")
    val name: String,

    @field:NotNull(message = "Price is required")
    @field:Positive(message = "Price must be greater than 0")
    @field:DecimalMax("999999.99", message = "Price is too large")
    val price: Double,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    @field:Size(max = 500, message = "Image URL cannot exceed 500 characters")
    val imageUrl: String? = null,

    @field:NotNull(message = "Restaurant ID is required")
    @field:Positive(message = "Restaurant ID must be positive")
    val restaurantId: Long? = null,

    val categoryId: Long? = null,

    val isVegetarian: Boolean = false,

    val isSpicy: Boolean = false
)

