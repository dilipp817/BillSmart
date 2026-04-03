package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Food
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FoodRepository : JpaRepository<Food, Long> {
    fun findByRestaurantRestroId(restroId: Long): List<Food>

    /**
     * Full-text search by name across all restaurants
     */
    @Query("""
        SELECT f FROM Food f
        WHERE (:query IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:restaurantId IS NULL OR f.restaurant.restroId = :restaurantId)
          AND (:categoryId IS NULL OR f.categoryId = :categoryId)
          AND (:isVegetarian IS NULL OR f.isVegetarian = :isVegetarian)
          AND (:isSpicy IS NULL OR f.isSpicy = :isSpicy)
          AND (:isAvailable IS NULL OR f.isAvailable = :isAvailable)
        ORDER BY f.name ASC
    """)
    fun searchFoods(
        @Param("query") query: String?,
        @Param("restaurantId") restaurantId: Long?,
        @Param("categoryId") categoryId: Long?,
        @Param("isVegetarian") isVegetarian: Boolean?,
        @Param("isSpicy") isSpicy: Boolean?,
        @Param("isAvailable") isAvailable: Boolean?
    ): List<Food>
}
