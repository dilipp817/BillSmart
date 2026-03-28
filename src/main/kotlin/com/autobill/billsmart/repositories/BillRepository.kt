package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Bill
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * BillRepository - Data access for Bill entities
 */
@Repository
interface BillRepository : JpaRepository<Bill, Long> {

    /**
     * Find bill by bill number
     */
    fun findByBillNumber(billNumber: String): Bill?

    /**
     * Find bills by order ID
     */
    fun findByOrderId(orderId: Long): List<Bill>

    /**
     * Find bills by restaurant ID with pagination
     */
    @Query("SELECT b FROM Bill b WHERE b.restaurant.restroId = :restaurantId ORDER BY b.createdAt DESC")
    fun findByRestaurantIdOrderByCreatedAtDesc(
        @org.springframework.data.repository.query.Param("restaurantId") restaurantId: Long,
        pageable: Pageable
    ): Page<Bill>

    /**
     * Find bills by status
     */
    @Query("SELECT b FROM Bill b WHERE b.status = :status ORDER BY b.createdAt DESC")
    fun findByStatusOrderByCreatedAtDesc(
        @org.springframework.data.repository.query.Param("status") status: String,
        pageable: Pageable
    ): Page<Bill>

    /**
     * Find bills by restaurant and status
     */
    @Query("SELECT b FROM Bill b WHERE b.restaurant.restroId = :restaurantId AND b.status = :status ORDER BY b.createdAt DESC")
    fun findByRestaurantIdAndStatusOrderByCreatedAtDesc(
        @org.springframework.data.repository.query.Param("restaurantId") restaurantId: Long,
        @org.springframework.data.repository.query.Param("status") status: String,
        pageable: Pageable
    ): Page<Bill>

    /**
     * Check if bill exists for order
     */
    fun existsByOrderId(orderId: Long): Boolean

    /**
     * Get last bill number by year for auto-increment
     */
    @Query("SELECT b.billNumber FROM Bill b WHERE YEAR(b.createdAt) = :year ORDER BY b.id DESC LIMIT 1")
    fun findLastBillNumberByYear(year: Int): String?

    /**
     * Count bills by restaurant
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.restaurant.restroId = :restaurantId")
    fun countByRestaurantId(@org.springframework.data.repository.query.Param("restaurantId") restaurantId: Long): Long

    /**
     * Count bills by status
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    fun countByStatus(@org.springframework.data.repository.query.Param("status") status: String): Long
}

