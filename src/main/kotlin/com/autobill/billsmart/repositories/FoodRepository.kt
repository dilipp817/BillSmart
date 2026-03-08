package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Food
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodRepository : JpaRepository<Food, Long> {
    fun findByRestaurantRestroId(restroId: Long): List<Food>
}
