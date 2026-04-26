package com.autobill.billsmart.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Category Request DTO
 * Used for creating/updating categories
 */
data class CategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    val name: String,

    val description: String? = null,

    val imageUrl: String? = null,

    val displayOrder: Int = 0
)

/**
 * Category Response DTO
 * Returns category information
 */
data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val foodCount: Int = 0,
    /** Restaurant this category belongs to. Used for tenant ownership verification. */
    val restaurantId: Long? = null
)

/**
 * Category List Item DTO
 * Lightweight version for list endpoints
 */
data class CategoryListItem(
    val id: Long,
    val name: String,
    val imageUrl: String? = null,
    val displayOrder: Int = 0,
    val foodCount: Int = 0
)

