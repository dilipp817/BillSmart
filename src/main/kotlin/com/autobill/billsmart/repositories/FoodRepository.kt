package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Food
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FoodRepository : JpaRepository<Food, Long> {
    fun findByRestaurantRestroIdAndIsDeletedFalse(restroId: Long): List<Food>

    fun findByIdAndIsDeletedFalse(id: Long): Food?

    /**
     * Full-text search by name across all restaurants — excludes soft-deleted items
     */
    @Query("""
        SELECT food FROM Food food
        WHERE food.isDeleted = FALSE
          AND (:query IS NULL OR LOWER(food.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:restaurantId IS NULL OR food.restaurant.restroId = :restaurantId)
          AND (:categoryId IS NULL OR food.categoryId = :categoryId)
          AND (:isVegetarian IS NULL OR food.isVegetarian = :isVegetarian)
          AND (:isSpicy IS NULL OR food.isSpicy = :isSpicy)
          AND (:isAvailable IS NULL OR food.isAvailable = :isAvailable)
        ORDER BY food.name ASC
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
