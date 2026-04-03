package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Payment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * PaymentRepository - Data access for Payment entities
 */
@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    /**
     * Find payment by reference number
     */
    fun findByReferenceNumber(referenceNumber: String): Payment?

    /**
     * Find payments by bill ID
     */
    fun findByBillIdOrderByCreatedAtDesc(billId: Long): List<Payment>

    /**
     * Find payments by bill ID with pagination
     */
    fun findByBillIdOrderByCreatedAtDesc(billId: Long, pageable: Pageable): Page<Payment>

    /**
     * Find payments by order ID with pagination
     */
    fun findByOrderIdOrderByCreatedAtDesc(orderId: Long, pageable: Pageable): Page<Payment>

    /**
     * Find payments by status with pagination
     */
    fun findByStatusOrderByCreatedAtDesc(status: String, pageable: Pageable): Page<Payment>

    /**
     * Find payments by payment method
     */
    fun findByPaymentMethodOrderByCreatedAtDesc(paymentMethod: String, pageable: Pageable): Page<Payment>

    /**
     * Check if payment exists for order
     */
    fun existsByOrderId(orderId: Long): Boolean

    /**
     * Count payments by status
     */
    fun countByStatus(status: String): Long

    /**
     * Find latest payment for order
     */
    fun findFirstByOrderIdOrderByCreatedAtDesc(orderId: Long): Payment?
}

