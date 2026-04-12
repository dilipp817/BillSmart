package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.dto.RestaurantResponse
import com.autobill.billsmart.model.Restaurant
import com.autobill.billsmart.mappers.RestaurantMapper
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.services.RestaurantService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RestaurantServiceImpl(
    private val restaurantRepository: RestaurantRepository,
    private val restaurantMapper: RestaurantMapper
) : RestaurantService {

    @Transactional
    override fun createRestaurant(req: RestaurantRequest): Restaurant {
        val restaurant = restaurantMapper.toRestaurant(req)
        return restaurantRepository.save(restaurant)
    }

    @Transactional(readOnly = true)
    override fun getRestaurant(id: Long): Restaurant? =
        restaurantRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    override fun getAllRestaurants(): List<Restaurant> =
        restaurantRepository.findAll()

    override fun toResponse(r: Restaurant) = RestaurantResponse(
        id            = r.restroId!!,
        outletName    = r.outletName,
        displayname   = r.displayname,
        outletManager = r.outletManager,
        building      = r.storeAddress?.building,
        street        = r.storeAddress?.street,
        storeLocation = r.storeAddress?.storelocation,
        zipCode       = r.storeAddress?.zipCode,
        createdAt     = r.createdAt,
        updatedAt     = r.updatedAt
    )
}
