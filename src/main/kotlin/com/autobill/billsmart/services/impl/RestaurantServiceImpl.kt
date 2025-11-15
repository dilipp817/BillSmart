package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.model.Restaurant
import com.autobill.billsmart.mappers.RestaurantMapper
import com.autobill.billsmart.ports.RestaurantRepositoryPort
import com.autobill.billsmart.services.RestaurantService
import org.springframework.stereotype.Service

@Service
class RestaurantServiceImpl(
    private val restaurantRepositoryPort: RestaurantRepositoryPort,
    private val restaurantMapper: RestaurantMapper
) : RestaurantService {

    override fun createRestaurant(req: RestaurantRequest): Restaurant {
        val restaurant = restaurantMapper.toRestaurant(req)
        return restaurantRepositoryPort.save(restaurant)
    }

    override fun getRestaurant(id: Long): Restaurant? {
        return restaurantRepositoryPort.findById(id)
    }
}

