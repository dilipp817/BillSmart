package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    /**
     * Find all categories for a restaurant, ordered by display_order
     */
    @Query("SELECT c FROM Category c WHERE c.restaurant.restroId = :restaurantId ORDER BY c.displayOrder ASC")
    fun findByRestaurantId(restaurantId: Long): List<Category>

    /**
     * Find all active categories for a restaurant
     */
    @Query("SELECT c FROM Category c WHERE c.restaurant.restroId = :restaurantId AND c.isActive = true ORDER BY c.displayOrder ASC")
    fun findActiveByRestaurantId(restaurantId: Long): List<Category>

    /**
     * Find category by name for a restaurant
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.restaurant.restroId = :restaurantId")
    fun findByNameAndRestaurantId(
        @org.springframework.data.repository.query.Param("name") name: String,
        @org.springframework.data.repository.query.Param("restaurantId") restaurantId: Long
    ): Category?
}

