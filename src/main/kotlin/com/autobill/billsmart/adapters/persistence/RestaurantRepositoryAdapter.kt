package com.autobill.billsmart.adapters.persistence

import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.model.Restaurant
import com.autobill.billsmart.ports.RestaurantRepositoryPort
import com.autobill.billsmart.repositories.RestaurantRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class RestaurantRepositoryAdapter(
    private val restaurantRepository: RestaurantRepository
) : RestaurantRepositoryPort {

    private val log = LoggerFactory.getLogger(RestaurantRepositoryAdapter::class.java)

    override fun save(restaurant: Restaurant): Restaurant {
        return try {
            val saved = restaurantRepository.save(restaurant)
            log.debug("Saved restaurant id={}", saved.restroId)
            saved
        } catch (ex: DataAccessException) {
            log.error("Database error saving restaurant", ex)
            throw AppException.PersistenceException("Failed to save restaurant", ex)
        }
    }

    override fun findById(id: Long): Restaurant? {
        return try {
            val found = restaurantRepository.findById(id).orElse(null)
            if (found == null) {
                log.debug("Restaurant not found for id={}", id)
            } else {
                log.debug("Found restaurant id={}", found.restroId)
            }
            found
        } catch (ex: DataAccessException) {
            log.error("Database error finding restaurant id={}", id, ex)
            throw AppException.PersistenceException("Failed to find restaurant", ex)
        }
    }
}

