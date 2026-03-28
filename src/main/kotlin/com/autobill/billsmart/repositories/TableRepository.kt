package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Table
import com.autobill.billsmart.model.enums.TableStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * TableRepository - Data access layer for Table entity
 *
 * Design Patterns:
 * - Repository Pattern: Abstracts data access logic
 * - Custom Queries: Optimized for specific use cases
 * - Query Performance: Uses indexes for fast lookups
 *
 * Thread Safety: Spring Data JPA handles concurrent access
 * Caching Strategy: Can be used with Spring Cache if needed
 */
@Repository
interface TableRepository : JpaRepository<Table, Long> {

    /**
     * Find all tables for a specific restaurant
     * Uses index: idx_tables_restaurant
     *
     * @param restaurantId the restaurant ID
     * @return list of tables in the restaurant
     */
    @Query(
        """
        SELECT t FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        ORDER BY t.tableNumber ASC
        """
    )
    fun findByRestaurantIdOrderByTableNumber(@Param("restaurantId") restaurantId: Long): List<Table>

    /**
     * Find available tables in restaurant with minimum capacity
     * Optimized query for seating customers
     * Uses index: idx_tables_status, idx_tables_restaurant
     *
     * @param restaurantId the restaurant ID
     * @param minCapacity minimum number of seats
     * @return list of available tables with sufficient capacity
     */
    @Query(
        """
        SELECT t FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.status = 'AVAILABLE' 
        AND t.capacity >= :minCapacity 
        ORDER BY t.capacity ASC
        """
    )
    fun findAvailableTablesWithCapacity(
        @Param("restaurantId") restaurantId: Long,
        @Param("minCapacity") minCapacity: Int
    ): List<Table>

    /**
     * Find tables by status in restaurant
     * Uses index: idx_tables_status
     *
     * @param restaurantId the restaurant ID
     * @param status the table status
     * @return list of tables with given status
     */
    @Query(
        """
        SELECT t FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.status = :status 
        ORDER BY t.tableNumber ASC
        """
    )
    fun findByRestaurantIdAndStatus(
        @Param("restaurantId") restaurantId: Long,
        @Param("status") status: TableStatus
    ): List<Table>

    /**
     * Find table by restaurant and table number (unique combination)
     * Uses index: idx_tables_restaurant_number
     *
     * @param restaurantId the restaurant ID
     * @param tableNumber the table number (e.g., "T1", "T2")
     * @return the table, or null if not found
     */
    @Query(
        """
        SELECT t FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.tableNumber = :tableNumber
        """
    )
    fun findByRestaurantIdAndTableNumber(
        @Param("restaurantId") restaurantId: Long,
        @Param("tableNumber") tableNumber: String
    ): Table?

    /**
     * Check if table number exists in restaurant
     * Lightweight query for validation
     *
     * @param restaurantId the restaurant ID
     * @param tableNumber the table number
     * @return true if table number exists
     */
    @Query(
        """
        SELECT COUNT(t) > 0 FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.tableNumber = :tableNumber
        """
    )
    fun existsByRestaurantIdAndTableNumber(
        @Param("restaurantId") restaurantId: Long,
        @Param("tableNumber") tableNumber: String
    ): Boolean

    /**
     * Find all occupied tables in restaurant (currently in use)
     * Uses index: idx_tables_restaurant, idx_tables_status
     *
     * @param restaurantId the restaurant ID
     * @return list of occupied tables
     */
    @Query(
        """
        SELECT t FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.status = 'OCCUPIED'
        """
    )
    fun findOccupiedTables(@Param("restaurantId") restaurantId: Long): List<Table>

    /**
     * Count available tables in restaurant
     * Fast count operation for availability check
     *
     * @param restaurantId the restaurant ID
     * @return number of available tables
     */
    @Query(
        """
        SELECT COUNT(t) FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.status = 'AVAILABLE'
        """
    )
    fun countAvailableTables(@Param("restaurantId") restaurantId: Long): Long

    /**
     * Find tables matching multiple criteria
     * Advanced search capability
     *
     * @param restaurantId the restaurant ID
     * @param statuses list of statuses to include
     * @return tables matching criteria
     */
    @Query(
        """
        SELECT t FROM Table t 
        WHERE t.restaurant.restroId = :restaurantId 
        AND t.status IN :statuses 
        ORDER BY t.tableNumber ASC
        """
    )
    fun findByRestaurantIdAndStatusIn(
        @Param("restaurantId") restaurantId: Long,
        @Param("statuses") statuses: List<TableStatus>
    ): List<Table>
}

