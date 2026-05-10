package com.autobill.billsmart.services

import com.autobill.billsmart.dto.FoodRequest
import com.autobill.billsmart.dto.FoodResponse

interface FoodService {
    fun createFood(restroId: Long, req: FoodRequest): FoodResponse
    fun updateFood(id: Long, req: FoodRequest): FoodResponse
    fun getAllFoods(restroId: Long): List<FoodResponse>
    fun getFood(id: Long): FoodResponse?
    fun deleteFood(id: Long)

    /**
     * Search foods with optional filters
     */
    fun searchFoods(
        query: String?,
        restaurantId: Long?,
        categoryId: Long?,
        isVegetarian: Boolean?,
        isSpicy: Boolean?,
        isAvailable: Boolean?
    ): List<FoodResponse>
}
