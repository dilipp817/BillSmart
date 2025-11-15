package com.autobill.billsmart.services

import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.model.Restaurant

interface RestaurantService {
    fun createRestaurant(req: RestaurantRequest): Restaurant
    fun getRestaurant(id: Long): Restaurant?
}

