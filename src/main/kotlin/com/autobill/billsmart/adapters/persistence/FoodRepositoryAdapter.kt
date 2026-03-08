package com.autobill.billsmart.adapters.persistence

import com.autobill.billsmart.model.Food
import com.autobill.billsmart.ports.FoodRepositoryPort
import com.autobill.billsmart.repositories.FoodRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class FoodRepositoryAdapter(
    private val foodRepository: FoodRepository
) : FoodRepositoryPort {

    private val log = LoggerFactory.getLogger(FoodRepositoryAdapter::class.java)

    override fun save(food: Food): Food {
        val saved = foodRepository.save(food)
        log.debug("Saved food id={}", saved.id)
        return saved
    }

    override fun findById(id: Long): Food? {
        val found = foodRepository.findById(id).orElse(null)
        if (found == null) log.debug("Food not found for id={}", id) else log.debug("Found food id={}", found.id)
        return found
    }

    override fun findByRestaurantRestroId(restroId: Long): List<Food> {
        val foods = foodRepository.findByRestaurantRestroId(restroId)
        log.debug("Found {} foods for restaurant id={}", foods.size, restroId)
        return foods
    }
}

