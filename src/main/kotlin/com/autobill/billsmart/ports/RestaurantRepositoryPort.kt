package com.autobill.billsmart.ports

import com.autobill.billsmart.model.Restaurant

interface RestaurantRepositoryPort {
    fun save(restaurant: Restaurant): Restaurant
    fun findById(id: Long): Restaurant?
}

