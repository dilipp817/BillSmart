package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Order
import com.autobill.billsmart.model.enums.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * OrderRepository - Data access layer for Order entity
 *
 * Design Patterns:
 * - Repository Pattern: Abstracts data access logic
 * - Custom Queries: Optimized for specific use cases
 * - Query Performance: Uses indexes for fast lookups
 *
 * Thread Safety: Spring Data JPA handles concurrent access
 */
@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific restaurant
     * Uses index: idx_orders_restaurant
     *
     * @param restaurantId the restaurant ID
     * @return list of orders in the restaurant
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.restaurant.id = :restaurantId 
        ORDER BY o.createdAt DESC
        """
    )
    fun findByRestaurantIdOrderByCreatedAtDesc(@Param("restaurantId") restaurantId: Long): List<Order>

    /**
     * Find orders by status for a restaurant
     * Uses index: idx_orders_status
     *
     * @param restaurantId the restaurant ID
     * @param status the order status
     * @return list of orders with given status
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.restaurant.id = :restaurantId 
        AND o.status = :status 
        ORDER BY o.createdAt DESC
        """
    )
    fun findByRestaurantIdAndStatus(
        @Param("restaurantId") restaurantId: Long,
        @Param("status") status: OrderStatus
    ): List<Order>

    /**
     * Find all active orders (not final state) for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @return list of active orders
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.restaurant.id = :restaurantId 
        AND o.status NOT IN ('DELIVERED', 'CANCELLED') 
        ORDER BY o.createdAt ASC
        """
    )
    fun findActiveOrdersByRestaurantId(@Param("restaurantId") restaurantId: Long): List<Order>

    /**
     * Find order by order number
     *
     * @param orderNumber the order number
     * @return the order, or null if not found
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.orderNumber = :orderNumber
        """
    )
    fun findByOrderNumber(@Param("orderNumber") orderNumber: String): Order?

    /**
     * Find orders for a specific table
     * Uses index: idx_orders_table
     *
     * @param tableId the table ID
     * @return list of orders for the table
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.table.id = :tableId 
        ORDER BY o.createdAt DESC
        """
    )
    fun findByTableIdOrderByCreatedAtDesc(@Param("tableId") tableId: Long): List<Order>

    /**
     * Find active order for a table (if any)
     *
     * @param tableId the table ID
     * @return the active order, or null if none
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.table.id = :tableId 
        AND o.status NOT IN ('DELIVERED', 'CANCELLED') 
        ORDER BY o.createdAt DESC
        LIMIT 1
        """
    )
    fun findActiveOrderByTableId(@Param("tableId") tableId: Long): Order?

    /**
     * Find orders by date range for reporting
     *
     * @param restaurantId the restaurant ID
     * @param startDate start date
     * @param endDate end date
     * @return list of orders in date range
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.restaurant.id = :restaurantId 
        AND o.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY o.createdAt DESC
        """
    )
    fun findByRestaurantIdAndDateRange(
        @Param("restaurantId") restaurantId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Order>

    /**
     * Count pending orders for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @return count of pending orders
     */
    @Query(
        """
        SELECT COUNT(o) FROM Order o 
        WHERE o.restaurant.id = :restaurantId 
        AND o.status = 'PENDING'
        """
    )
    fun countPendingOrders(@Param("restaurantId") restaurantId: Long): Long

    /**
     * Find orders by multiple statuses
     *
     * @param restaurantId the restaurant ID
     * @param statuses list of statuses
     * @return orders matching criteria
     */
    @Query(
        """
        SELECT o FROM Order o 
        WHERE o.restaurant.id = :restaurantId 
        AND o.status IN :statuses 
        ORDER BY o.createdAt DESC
        """
    )
    fun findByRestaurantIdAndStatusIn(
        @Param("restaurantId") restaurantId: Long,
        @Param("statuses") statuses: List<OrderStatus>
    ): List<Order>

    /**
     * Check if order exists by order number
     *
     * @param orderNumber the order number
     * @return true if exists
     */
    @Query(
        """
        SELECT COUNT(o) > 0 FROM Order o 
        WHERE o.orderNumber = :orderNumber
        """
    )
    fun existsByOrderNumber(@Param("orderNumber") orderNumber: String): Boolean
}

