package com.autobill.billsmart.ports

import com.autobill.billsmart.model.Food

interface FoodRepositoryPort {
    fun save(food: Food): Food
    fun findById(id: Long): Food?
    fun findByRestaurantRestroId(restroId: Long): List<Food>
}

