package com.autobill.billsmart.dto

import java.time.LocalDateTime

/**
 * Food Response DTO
 * Complete food item information for API responses
 */
data class FoodResponse(
    val id: Long,
    val name: String,
    val price: Double,
    val description: String? = null,
    val imageUrl: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val restaurantId: Long,
    val restaurantName: String? = null,
    val isAvailable: Boolean = true,
    val preparationTime: Int? = null,
    val allergens: String? = null,
    val calories: Int? = null,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * Food List Item DTO
 * Lightweight version for list endpoints
 */
data class FoodListItem(
    val id: Long,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val categoryName: String? = null,
    val isAvailable: Boolean = true,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false
)
