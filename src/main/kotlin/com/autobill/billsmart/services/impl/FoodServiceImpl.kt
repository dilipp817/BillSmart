package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.FoodRequest
import com.autobill.billsmart.dto.FoodResponse
import com.autobill.billsmart.mappers.FoodMapper
import com.autobill.billsmart.ports.FoodRepositoryPort
import com.autobill.billsmart.ports.RestaurantRepositoryPort
import com.autobill.billsmart.services.FoodService
import org.springframework.stereotype.Service

@Service
class FoodServiceImpl(
    private val foodRepositoryPort: FoodRepositoryPort,
    private val restaurantRepositoryPort: RestaurantRepositoryPort,
    private val foodMapper: FoodMapper
) : FoodService {

    override fun createFood(restroId: Long, req: FoodRequest): FoodResponse {
        val restaurant = restaurantRepositoryPort.findById(restroId)
            ?: throw IllegalArgumentException("Restaurant not found: $restroId")

        val food = foodMapper.toFood(req)
        food.restaurant = restaurant
        val saved = foodRepositoryPort.save(food)
        return foodMapper.toResponse(saved)
    }

    override fun getAllFoods(restroId: Long): List<FoodResponse> {
        restaurantRepositoryPort.findById(restroId)
            ?: throw IllegalArgumentException("Restaurant not found: $restroId")

        val foods = foodRepositoryPort.findByRestaurantRestroId(restroId)
        return foods.map { foodMapper.toResponse(it) }
    }

    override fun getFood(id: Long): FoodResponse? =
        foodRepositoryPort.findById(id)?.let { foodMapper.toResponse(it) }
}
