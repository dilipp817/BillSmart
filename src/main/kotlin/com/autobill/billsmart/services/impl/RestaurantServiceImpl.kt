package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.AddressResponse
import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.dto.RestaurantResponse
import com.autobill.billsmart.model.Address
import com.autobill.billsmart.model.Restaurant
import com.autobill.billsmart.mappers.RestaurantMapper
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.services.RestaurantService
import com.autobill.billsmart.exception.AppException
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

    @Transactional
    override fun updateRestaurant(id: Long, req: RestaurantRequest): Restaurant {
        val restaurant = restaurantRepository.findById(id).orElseThrow {
            AppException.ResourceNotFoundException("Restaurant not found with ID: $id")
        }
        restaurant.outletName    = req.outletName
        restaurant.displayname   = req.displayname
        restaurant.outletManager = req.outletManager
        restaurant.storeAddress  = Address(
            building = req.storeAddress.building,
            street   = req.storeAddress.street,
            location = req.storeAddress.location,
            zipCode  = req.storeAddress.zipCode
        )
        return restaurantRepository.save(restaurant)
    }

    override fun toResponse(r: Restaurant) = RestaurantResponse(
        id            = r.restroId!!,
        outletName    = r.outletName,
        displayname   = r.displayname,
        outletManager = r.outletManager,
        address       = r.storeAddress?.let {
            AddressResponse(
                building = it.building,
                street   = it.street,
                location = it.location,
                zipCode  = it.zipCode
            )
        },
        createdAt     = r.createdAt,
        updatedAt     = r.updatedAt
    )
}
