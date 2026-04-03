package com.autobill.billsmart.services

import com.autobill.billsmart.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * BillService - Interface for bill operations
 */
interface BillService {

    /**
     * Create bill from order
     *
     * @param request bill creation request
     * @return created bill response
     */
    fun createBill(request: BillRequest): BillResponse

    /**
     * Get bill by ID
     *
     * @param id bill ID
     * @return bill response or null if not found
     */
    fun getBillById(id: Long): BillResponse?

    /**
     * Get bill by number
     *
     * @param billNumber bill number
     * @return bill response or null if not found
     */
    fun getBillByNumber(billNumber: String): BillResponse?

    /**
     * Get all bills for restaurant (paginated)
     *
     * @param restaurantId restaurant ID
     * @param pageable pagination info
     * @return page of bills
     */
    fun getBillsByRestaurant(restaurantId: Long, pageable: Pageable): Page<BillListResponse>

    /**
     * Get bills by status (paginated)
     *
     * @param status bill status
     * @param pageable pagination info
     * @return page of bills
     */
    fun getBillsByStatus(status: String, pageable: Pageable): Page<BillListResponse>

    /**
     * Update bill
     *
     * @param id bill ID
     * @param request update request
     * @return updated bill response
     */
    fun updateBill(id: Long, request: BillRequest): BillResponse?

    /**
     * Delete bill
     *
     * @param id bill ID
     * @return true if deleted, false otherwise
     */
    fun deleteBill(id: Long): Boolean

    /**
     * Mark bill as paid
     *
     * @param id bill ID
     * @return updated bill response
     */
    fun markBillAsPaid(id: Long): BillResponse?

    /**
     * Cancel bill
     *
     * @param id bill ID
     * @return updated bill response
     */
    fun cancelBill(id: Long): BillResponse?

    /**
     * Add items to bill
     *
     * @param billId bill ID
     * @param items bill items to add
     * @return updated bill response
     */
    fun addBillItems(billId: Long, items: List<BillItemRequest>): BillResponse?

    /**
     * Remove item from bill
     *
     * @param billId bill ID
     * @param itemId bill item ID
     * @return updated bill response
     */
    fun removeBillItem(billId: Long, itemId: Long): BillResponse?

    /**
     * Auto-generate a bill for an order.
     * Backend calculates subtotal, CGST (9%), SGST (9%), total.
     * Bill number is auto-generated. No manual calculation needed by client.
     *
     * @param orderId order ID
     * @param discountAmount optional discount (default 0)
     * @return generated bill response
     */
    fun generateBillForOrder(orderId: Long, discountAmount: java.math.BigDecimal = java.math.BigDecimal.ZERO): BillResponse
}

