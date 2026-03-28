package com.autobill.billsmart.services

import com.autobill.billsmart.dto.CategoryRequest
import com.autobill.billsmart.dto.CategoryResponse
import com.autobill.billsmart.model.Category

interface CategoryService {

    /**
     * Get all categories for a restaurant (only active ones)
     */
    fun getCategories(restaurantId: Long): List<CategoryResponse>

    /**
     * Get category by ID
     */
    fun getCategory(id: Long): CategoryResponse?

    /**
     * Create a new category
     */
    fun createCategory(restaurantId: Long, request: CategoryRequest): CategoryResponse

    /**
     * Update a category
     */
    fun updateCategory(id: Long, request: CategoryRequest): CategoryResponse?

    /**
     * Delete a category
     */
    fun deleteCategory(id: Long): Boolean
}

