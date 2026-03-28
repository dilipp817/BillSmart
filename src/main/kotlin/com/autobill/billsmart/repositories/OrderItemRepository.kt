package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * OrderItemRepository - Data access layer for OrderItem entity
 */
@Repository
interface OrderItemRepository : JpaRepository<OrderItem, Long> {

    /**
     * Find all items for an order
     *
     * @param orderId the order ID
     * @return list of order items
     */
    @Query(
        """
        SELECT oi FROM OrderItem oi 
        WHERE oi.order.id = :orderId 
        ORDER BY oi.createdAt ASC
        """
    )
    fun findByOrderIdOrderByCreatedAtAsc(@Param("orderId") orderId: Long): List<OrderItem>

    /**
     * Find items by status in order
     *
     * @param orderId the order ID
     * @param status the item status
     * @return list of items with given status
     */
    @Query(
        """
        SELECT oi FROM OrderItem oi 
        WHERE oi.order.id = :orderId 
        AND oi.itemStatus = :status
        """
    )
    fun findByOrderIdAndStatus(
        @Param("orderId") orderId: Long,
        @Param("status") status: String
    ): List<OrderItem>

    /**
     * Count items in order
     *
     * @param orderId the order ID
     * @return count of items
     */
    @Query(
        """
        SELECT COUNT(oi) FROM OrderItem oi 
        WHERE oi.order.id = :orderId
        """
    )
    fun countByOrderId(@Param("orderId") orderId: Long): Long

    /**
     * Find pending items in order
     *
     * @param orderId the order ID
     * @return list of pending items
     */
    @Query(
        """
        SELECT oi FROM OrderItem oi 
        WHERE oi.order.id = :orderId 
        AND oi.itemStatus IN ('PENDING', 'IN_PROGRESS')
        """
    )
    fun findPendingItemsByOrderId(@Param("orderId") orderId: Long): List<OrderItem>

    /**
     * Check if all items are served
     *
     * @param orderId the order ID
     * @return true if all items are served
     */
    @Query(
        """
        SELECT COUNT(oi) FROM OrderItem oi 
        WHERE oi.order.id = :orderId 
        AND oi.itemStatus != 'SERVED' 
        AND oi.itemStatus != 'CANCELLED'
        """
    )
    fun countUnservedItems(@Param("orderId") orderId: Long): Long
}

