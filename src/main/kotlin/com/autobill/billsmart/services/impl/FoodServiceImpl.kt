package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.FoodRequest
import com.autobill.billsmart.dto.FoodResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.mappers.FoodMapper
import com.autobill.billsmart.repositories.FoodRepository
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.services.FoodService
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodServiceImpl(
    private val foodRepository: FoodRepository,
    private val restaurantRepository: RestaurantRepository,
    private val foodMapper: FoodMapper
) : FoodService {

    @Transactional
    override fun createFood(restroId: Long, req: FoodRequest): FoodResponse {
        val restaurant = restaurantRepository.findById(restroId).orElse(null)
            ?: throw AppException.ResourceNotFoundException("Restaurant not found: $restroId")

        val food = foodMapper.toFood(req)
        food.restaurant = restaurant
        val saved = foodRepository.save(food)
        return foodMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun getAllFoods(restroId: Long): List<FoodResponse> {
        restaurantRepository.findById(restroId).orElse(null)
            ?: throw AppException.ResourceNotFoundException("Restaurant not found: $restroId")

        val foods = foodRepository.findByRestaurantRestroIdAndIsDeletedFalse(restroId)
        return foods.map { foodMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getFood(id: Long): FoodResponse? =
        foodRepository.findByIdAndIsDeletedFalse(id)?.let { foodMapper.toResponse(it) }

    @Transactional(readOnly = true)
    override fun searchFoods(
        query: String?,
        restaurantId: Long?,
        categoryId: Long?,
        isVegetarian: Boolean?,
        isSpicy: Boolean?,
        isAvailable: Boolean?
    ): List<FoodResponse> {
        val results = foodRepository.searchFoods(
            query = query?.trim()?.ifBlank { null },
            restaurantId = restaurantId,
            categoryId = categoryId,
            isVegetarian = isVegetarian,
            isSpicy = isSpicy,
            isAvailable = isAvailable
        )
        return results.map { foodMapper.toResponse(it) }
    }

    @Transactional
    override fun deleteFood(id: Long) {
        val food = foodRepository.findByIdAndIsDeletedFalse(id)
            ?: throw AppException.ResourceNotFoundException("Food not found with ID: $id")
        food.isDeleted = true
        food.deletedAt = LocalDateTime.now()
        foodRepository.save(food)
    }
}
