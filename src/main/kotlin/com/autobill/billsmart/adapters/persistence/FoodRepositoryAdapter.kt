package com.autobill.billsmart.adapters.persistence

import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.model.Food
import com.autobill.billsmart.ports.FoodRepositoryPort
import com.autobill.billsmart.repositories.FoodRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class FoodRepositoryAdapter(
    private val foodRepository: FoodRepository
) : FoodRepositoryPort {

    private val log = LoggerFactory.getLogger(FoodRepositoryAdapter::class.java)

    override fun save(food: Food): Food {
        return try {
            val saved = foodRepository.save(food)
            log.debug("Saved food id={}", saved.id)
            saved
        } catch (ex: DataAccessException) {
            log.error("Database error saving food", ex)
            throw AppException.PersistenceException("Failed to save food", ex)
        }
    }

    override fun findById(id: Long): Food? {
        return try {
            val found = foodRepository.findById(id).orElse(null)
            if (found == null) {
                log.debug("Food not found for id={}", id)
            } else {
                log.debug("Found food id={}", found.id)
            }
            found
        } catch (ex: DataAccessException) {
            log.error("Database error finding food id={}", id, ex)
            throw AppException.PersistenceException("Failed to find food", ex)
        }
    }

    override fun findByRestaurantRestroId(restroId: Long): List<Food> {
        return try {
            val foods = foodRepository.findByRestaurantRestroId(restroId)
            log.debug("Found {} foods for restaurant id={}", foods.size, restroId)
            foods
        } catch (ex: DataAccessException) {
            log.error("Database error finding foods for restaurant id={}", restroId, ex)
            throw AppException.PersistenceException("Failed to find foods", ex)
        }
    }
}

