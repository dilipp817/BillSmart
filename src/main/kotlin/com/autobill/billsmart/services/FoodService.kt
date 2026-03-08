package com.autobill.billsmart.services

import com.autobill.billsmart.dto.FoodRequest
import com.autobill.billsmart.dto.FoodResponse

interface FoodService {
    fun createFood(restroId: Long, req: FoodRequest): FoodResponse
    fun getAllFoods(restroId: Long): List<FoodResponse>
    fun getFood(id: Long): FoodResponse?
}
