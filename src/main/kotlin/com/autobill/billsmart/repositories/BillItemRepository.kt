package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.BillItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * BillItemRepository - Data access for BillItem entities
 */
@Repository
interface BillItemRepository : JpaRepository<BillItem, Long> {

    /**
     * Find bill items by bill ID
     */
    fun findByBillId(billId: Long): List<BillItem>

    /**
     * Find bill items by food ID
     */
    fun findByFoodId(foodId: Long): List<BillItem>

    /**
     * Delete bill items by bill ID
     */
    fun deleteByBillId(billId: Long)

    /**
     * Count bill items by bill
     */
    fun countByBillId(billId: Long): Long
}

