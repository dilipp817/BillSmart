package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.mappers.BillMapper
import com.autobill.billsmart.model.Bill
import com.autobill.billsmart.model.BillItem
import com.autobill.billsmart.repositories.BillItemRepository
import com.autobill.billsmart.repositories.BillRepository
import com.autobill.billsmart.repositories.OrderRepository
import com.autobill.billsmart.repositories.PaymentRepository
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.services.BillService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * BillServiceImpl - Implementation of BillService
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles bill operations
 * - Open/Closed: Can be extended without modification
 * - Liskov Substitution: Properly implements BillService interface
 * - Interface Segregation: Depends on focused interfaces
 * - Dependency Inversion: Depends on abstractions
 *
 * Thread Safety:
 * - @Transactional ensures database consistency
 * - @Version field handles optimistic locking
 *
 * Business Logic:
 * - Auto-generates unique bill numbers
 * - Calculates GST taxes (18% split: 9% CGST + 9% SGST)
 * - Validates bill state transitions
 * - Prevents bill modification after payment
 */
@Service
@Transactional
class BillServiceImpl(
    private val billRepository: BillRepository,
    private val billItemRepository: BillItemRepository,
    private val orderRepository: OrderRepository,
    private val restaurantRepository: RestaurantRepository,
    private val paymentRepository: PaymentRepository,   // B-NEW-1
    private val billMapper: BillMapper
) : BillService {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE OPERATIONS ====================

    override fun createBill(request: BillRequest): BillResponse {
        logger.debug("Creating bill: billNumber={}", request.billNumber)

        // Validate order exists
        val order = orderRepository.findById(request.orderId)
            .orElseThrow {
                logger.error("Order not found: {}", request.orderId)
                AppException.ResourceNotFoundException("Order not found: ${request.orderId}")
            }

        // Validate restaurant exists
        val restaurant = restaurantRepository.findById(request.restaurantId)
            .orElseThrow {
                logger.error("Restaurant not found: {}", request.restaurantId)
                AppException.ResourceNotFoundException("Restaurant not found: ${request.restaurantId}")
            }

        // Check if bill already exists for order
        if (billRepository.existsByOrderId(request.orderId)) {
            logger.warn("Bill already exists for order: {}", request.orderId)
            throw AppException.ValidationException("Bill already exists for this order")
        }

        // Validate amounts
        if (request.totalAmount <= BigDecimal.ZERO) {
            logger.warn("Invalid total amount: {}", request.totalAmount)
            throw AppException.ValidationException("Total amount must be greater than 0")
        }

        // Create bill entity
        val bill = Bill().apply {
            this.billNumber = request.billNumber?.takeIf { it.isNotBlank() }
                ?: generateBillNumber(restaurant.restroId!!)
            this.order = order
            this.restaurant = restaurant
            this.subtotal = request.subtotal
            this.taxAmount = request.taxAmount
            this.cgstAmount = request.cgstAmount
            this.sgstAmount = request.sgstAmount
            this.discountAmount = request.discountAmount
            this.totalAmount = request.totalAmount
            this.status = request.status ?: "ISSUED"
        }

        val saved = billRepository.save(bill)
        logger.info("Bill created successfully: billNumber={}, id={}", saved.billNumber, saved.id)
        return enrichWithPaymentTotals(billMapper.toResponse(saved))
    }

    // ==================== READ OPERATIONS ====================

    @Transactional(readOnly = true)
    override fun getBillById(id: Long): BillResponse? {
        logger.debug("Fetching bill: {}", id)
        return billRepository.findById(id)
            .map { enrichWithPaymentTotals(billMapper.toResponse(it)) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun getBillByNumber(billNumber: String): BillResponse? {
        logger.debug("Fetching bill by number: {}", billNumber)
        return billRepository.findByBillNumber(billNumber)
            ?.let { enrichWithPaymentTotals(billMapper.toResponse(it)) }
    }

    @Transactional(readOnly = true)
    override fun getBillsByRestaurant(restaurantId: Long, pageable: Pageable): Page<BillListResponse> {
        logger.debug("Fetching bills for restaurant: {}", restaurantId)

        if (!restaurantRepository.existsById(restaurantId)) {
            logger.error("Restaurant not found: {}", restaurantId)
            throw AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
        }

        return billRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
            .map { billMapper.toListResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getBillsByRestaurantAndStatus(restaurantId: Long, status: String?, pageable: Pageable): Page<BillListResponse> {
        logger.debug("Fetching bills for restaurant: {} with status: {}", restaurantId, status)

        if (!restaurantRepository.existsById(restaurantId)) {
            throw AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
        }

        return if (status != null) {
            if (!isValidBillStatus(status)) throw AppException.ValidationException("Invalid bill status: $status")
            billRepository.findByRestaurantIdAndStatusOrderByCreatedAtDesc(restaurantId, status, pageable)
                .map { billMapper.toListResponse(it) }
        } else {
            billRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map { billMapper.toListResponse(it) }
        }
    }

    @Transactional(readOnly = true)
    override fun getBillsByStatus(status: String, pageable: Pageable): Page<BillListResponse> {
        logger.debug("Fetching bills by status: {}", status)

        // Validate status
        if (!isValidBillStatus(status)) {
            logger.warn("Invalid bill status: {}", status)
            throw AppException.ValidationException("Invalid bill status: $status")
        }

        return billRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map { billMapper.toListResponse(it) }
    }

    // ==================== UPDATE OPERATIONS ====================

    override fun updateBill(id: Long, request: BillRequest): BillResponse? {
        logger.debug("Updating bill: {}", id)

        val bill = billRepository.findById(id)
            .orElseThrow {
                logger.error("Bill not found: {}", id)
                AppException.ResourceNotFoundException("Bill not found: $id")
            }

        if (!bill.canBeModified()) {
            logger.warn("Bill cannot be modified - status: {}", bill.status)
            throw AppException.ValidationException("Bill cannot be modified in ${bill.status} status")
        }

        bill.subtotal = request.subtotal
        bill.taxAmount = request.taxAmount
        bill.cgstAmount = request.cgstAmount
        bill.sgstAmount = request.sgstAmount
        bill.discountAmount = request.discountAmount
        bill.totalAmount = request.totalAmount
        bill.updatedAt = LocalDateTime.now()

        val updated = billRepository.save(bill)
        logger.info("Bill updated: {}", id)
        return enrichWithPaymentTotals(billMapper.toResponse(updated))
    }

    override fun markBillAsPaid(id: Long): BillResponse? {
        logger.debug("Marking bill as paid: {}", id)

        val bill = billRepository.findById(id)
            .orElseThrow {
                logger.error("Bill not found: {}", id)
                AppException.ResourceNotFoundException("Bill not found: $id")
            }

        if (bill.status != "ISSUED" && bill.status != "PARTIAL") {
            logger.warn("Bill cannot be marked as paid - current status: {}", bill.status)
            throw AppException.ValidationException("Bill must be in ISSUED or PARTIAL status to mark as paid")
        }

        bill.markAsPaid()
        val updated = billRepository.save(bill)
        logger.info("Bill marked as paid: {}", id)
        return enrichWithPaymentTotals(billMapper.toResponse(updated))
    }

    override fun cancelBill(id: Long): BillResponse? {
        logger.debug("Cancelling bill: {}", id)

        val bill = billRepository.findById(id)
            .orElseThrow {
                logger.error("Bill not found: {}", id)
                AppException.ResourceNotFoundException("Bill not found: $id")
            }

        if (bill.status == "PAID" || bill.status == "CANCELLED") {
            logger.warn("Bill cannot be cancelled - current status: {}", bill.status)
            throw AppException.ValidationException("Bill cannot be cancelled in ${bill.status} status")
        }

        bill.cancel()
        val updated = billRepository.save(bill)
        logger.info("Bill cancelled: {}", id)
        return enrichWithPaymentTotals(billMapper.toResponse(updated))
    }

    // ==================== BILL ITEMS OPERATIONS ====================

    override fun addBillItems(billId: Long, items: List<BillItemRequest>): BillResponse? {
        logger.debug("Adding {} items to bill: {}", items.size, billId)

        val bill = billRepository.findById(billId)
            .orElseThrow {
                logger.error("Bill not found: {}", billId)
                AppException.ResourceNotFoundException("Bill not found: $billId")
            }

        if (!bill.canBeModified()) {
            logger.warn("Cannot add items to bill - status: {}", bill.status)
            throw AppException.ValidationException("Cannot modify bill in ${bill.status} status")
        }

        items.forEach { itemRequest ->
            val billItem = BillItem().apply {
                this.bill = bill
                this.quantity = itemRequest.quantity
                this.unitPrice = itemRequest.unitPrice
            }
            billItem.calculateTotal()
            bill.billItems.add(billItem)
        }

        bill.updatedAt = LocalDateTime.now()
        val updated = billRepository.save(bill)
        logger.info("Added {} items to bill: {}", items.size, billId)
        return enrichWithPaymentTotals(billMapper.toResponse(updated))
    }

    override fun removeBillItem(billId: Long, itemId: Long): BillResponse? {
        logger.debug("Removing item {} from bill: {}", itemId, billId)

        val bill = billRepository.findById(billId)
            .orElseThrow {
                logger.error("Bill not found: {}", billId)
                AppException.ResourceNotFoundException("Bill not found: $billId")
            }

        if (!bill.canBeModified()) {
            logger.warn("Cannot remove items from bill - status: {}", bill.status)
            throw AppException.ValidationException("Cannot modify bill in ${bill.status} status")
        }

        val item = bill.billItems.find { it.id == itemId }
            ?: throw AppException.ResourceNotFoundException("Bill item not found: $itemId")

        bill.billItems.remove(item)
        billItemRepository.deleteById(itemId)

        bill.updatedAt = LocalDateTime.now()
        val updated = billRepository.save(bill)
        logger.info("Removed item {} from bill: {}", itemId, billId)
        return enrichWithPaymentTotals(billMapper.toResponse(updated))
    }

    // ==================== DELETE OPERATIONS ====================

    override fun deleteBill(id: Long): Boolean {
        logger.debug("Deleting bill: {}", id)

        if (!billRepository.existsById(id)) {
            logger.warn("Bill not found for deletion: {}", id)
            return false
        }

        val bill = billRepository.findById(id).get()
        if (bill.status != "ISSUED") {
            logger.warn("Cannot delete bill in {} status", bill.status)
            throw AppException.ValidationException("Can only delete bills in ISSUED status")
        }

        billRepository.deleteById(id)
        logger.info("Bill deleted: {}", id)
        return true
    }

    // ==================== UTILITY METHODS ====================

    private fun isValidBillStatus(status: String): Boolean {
        return status in listOf("ISSUED", "PARTIAL", "PAID", "CANCELLED")
    }

    /**
     * Auto-generate a unique bill number.
     * Pattern: BILL-{restaurantId}-{yyyyMMdd}-{sequence}
     * Example: BILL-1-20260404-0001
     */
    private fun generateBillNumber(restaurantId: Long): String {
        val today = java.time.LocalDate.now()
        val dateStr = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        val year = today.year
        val lastBillNumber = billRepository.findLastBillNumberByYear(year)
        val sequence = if (lastBillNumber != null) {
            val parts = lastBillNumber.split("-")
            val lastSeq = parts.lastOrNull()?.toIntOrNull() ?: 0
            lastSeq + 1
        } else {
            1
        }
        return "BILL-$restaurantId-$dateStr-${sequence.toString().padStart(4, '0')}"
    }

    /**
     * Auto-generate a bill for an order.
     * Calculates subtotal from order items, applies 18% GST (9% CGST + 9% SGST).
     */
    override fun generateBillForOrder(orderId: Long, discountAmount: BigDecimal): BillResponse {
        logger.debug("Auto-generating bill for order: {}", orderId)

        // Only MANAGER and ADMIN can apply discounts — staff cannot.
        if (discountAmount > BigDecimal.ZERO) {
            val auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().authentication
            val role = auth?.authorities?.firstOrNull()?.authority ?: "ROLE_STAFF"
            if (role == "ROLE_STAFF") {
                throw AppException.ValidationException("Staff are not authorised to apply discounts")
            }
        }

        val order = orderRepository.findById(orderId)
            .orElseThrow {
                logger.error("Order not found: {}", orderId)
                AppException.ResourceNotFoundException("Order not found: $orderId")
            }

        val restaurant = order.restaurant
            ?: throw AppException.ValidationException("Order has no linked restaurant")

        // Prevent duplicate bills
        if (billRepository.existsByOrderId(orderId)) {
            logger.warn("Bill already exists for order: {}", orderId)
            throw AppException.ConflictException("A bill already exists for order: $orderId")
        }

        // Calculate amounts from order items
        val subtotal = order.totalAmount
        val taxes = calculateTaxes(subtotal)
        val totalAmount = subtotal + taxes.totalTax - discountAmount

        // Auto-generate bill number
        val billNumber = generateBillNumber(restaurant.restroId!!)

        val bill = Bill().apply {
            this.billNumber = billNumber
            this.order = order
            this.restaurant = restaurant
            this.subtotal = subtotal
            this.taxAmount = taxes.totalTax
            this.cgstAmount = taxes.cgst
            this.sgstAmount = taxes.sgst
            this.discountAmount = discountAmount
            this.totalAmount = totalAmount
            this.status = "ISSUED"
        }

        val saved = billRepository.save(bill)
        logger.info("Bill auto-generated: billNumber={}, total={}", saved.billNumber, saved.totalAmount)
        return enrichWithPaymentTotals(billMapper.toResponse(saved))
    }

    /**
     * Calculate GST taxes (18% total: 9% CGST + 9% SGST)
     */
    fun calculateTaxes(subtotal: BigDecimal): TaxCalculation {
        val taxRate = BigDecimal("18") // 18% total
        val totalTax = (subtotal * taxRate / BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP)
        val halfTax = (totalTax / BigDecimal("2"))
            .setScale(2, RoundingMode.HALF_UP)

        return TaxCalculation(
            totalTax = totalTax,
            cgst = halfTax,
            sgst = halfTax
        )
    }

    /**
     * Enriches a BillResponse with live paid_amount and remaining_amount
     * computed from SUCCESS payments for this bill. (B-NEW-1)
     *
     * Called on all detail-endpoint responses. NOT called for BillListResponse
     * (lightweight list DTO — intentionally excludes these fields).
     */
    private fun enrichWithPaymentTotals(response: BillResponse): BillResponse {
        val paidAmount = paymentRepository
            .findByBillIdOrderByCreatedAtDesc(response.id)
            .filter { it.status == "SUCCESS" }
            .map { it.amount }
            .fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }
            .setScale(2, RoundingMode.HALF_UP)
        val remainingAmount = (response.totalAmount - paidAmount)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP)
        return BillResponse(
            id = response.id,
            billNumber = response.billNumber,
            orderId = response.orderId,
            restaurantId = response.restaurantId,
            restaurantName = response.restaurantName,
            subtotal = response.subtotal,
            taxAmount = response.taxAmount,
            cgstAmount = response.cgstAmount,
            sgstAmount = response.sgstAmount,
            discountAmount = response.discountAmount,
            totalAmount = response.totalAmount,
            status = response.status,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            billItems = response.billItems,
            createdAt = response.createdAt,
            updatedAt = response.updatedAt
        )
    }

    data class TaxCalculation(
        val totalTax: BigDecimal,
        val cgst: BigDecimal,
        val sgst: BigDecimal
    )
}

