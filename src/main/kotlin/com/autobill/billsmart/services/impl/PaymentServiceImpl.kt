package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.mappers.PaymentMapper
import com.autobill.billsmart.model.Bill
import com.autobill.billsmart.model.Payment
import com.autobill.billsmart.repositories.BillRepository
import com.autobill.billsmart.repositories.OrderRepository
import com.autobill.billsmart.repositories.PaymentRepository
import com.autobill.billsmart.services.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * PaymentServiceImpl - Implementation of PaymentService
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles payment operations
 * - Open/Closed: Can be extended without modification
 * - Liskov Substitution: Properly implements PaymentService interface
 * - Interface Segregation: Depends on focused interfaces
 * - Dependency Inversion: Depends on abstractions
 *
 * Thread Safety:
 * - @Transactional ensures database consistency
 * - @Version field handles optimistic locking
 * - Idempotency through reference number checking
 *
 * Business Logic:
 * - Multiple payment methods support (CASH, CARD, UPI, WALLET)
 * - Payment status tracking (PENDING → SUCCESS/FAILED)
 * - Idempotent payment processing (prevents duplicates)
 * - Automatic bill status update on payment success
 */
@Service
@Transactional
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val billRepository: BillRepository,
    private val orderRepository: OrderRepository,
    private val paymentMapper: PaymentMapper
) : PaymentService {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE OPERATIONS ====================

    override fun createPayment(request: PaymentRequest): PaymentResponse {
        logger.debug("Creating payment - orderId: {}, amount: {}, method: {}",
            request.orderId, request.amount, request.paymentMethod)

        // Validate order exists
        val order = orderRepository.findById(request.orderId)
            .orElseThrow {
                logger.error("Order not found: {}", request.orderId)
                AppException.ResourceNotFoundException("Order not found: ${request.orderId}")
            }

        // Idempotency guard — deduplicate on ANY existing status, not just SUCCESS.
        // Scenario: mobile POSTs → network dies before response → mobile retries with same
        // reference_number. Without this guard, a second PENDING payment would be created
        // and both could be processed → double charge.
        val existingPayment = paymentRepository.findByReferenceNumber(request.referenceNumber)
        if (existingPayment != null) {
            logger.warn("Duplicate payment request for reference: {} (existing status: {})",
                request.referenceNumber, existingPayment.status)
            return paymentMapper.toResponse(existingPayment)
        }

        // Validate payment method
        if (!isValidPaymentMethod(request.paymentMethod)) {
            logger.warn("Invalid payment method: {}", request.paymentMethod)
            throw AppException.ValidationException("Invalid payment method: ${request.paymentMethod}")
        }

        // Validate amount
        if (request.amount <= BigDecimal.ZERO) {
            logger.warn("Invalid payment amount: {}", request.amount)
            throw AppException.ValidationException("Payment amount must be greater than 0")
        }

        // Get bill if provided
        val bill = request.billId?.let {
            billRepository.findById(it)
                .orElseThrow {
                    logger.error("Bill not found: {}", it)
                    AppException.ResourceNotFoundException("Bill not found: $it")
                }
        }

        // Create payment entity
        val payment = Payment().apply {
            this.bill = bill
            this.order = order
            this.paymentMethod = request.paymentMethod
            this.amount = request.amount
            this.status = "PENDING"
            this.transactionId = request.transactionId
            this.referenceNumber = request.referenceNumber
            this.notes = request.notes
            this.changeAmount = request.changeAmount
        }

        val saved = paymentRepository.save(payment)
        logger.info("Payment created - id: {}, reference: {}, amount: {}", saved.id, saved.referenceNumber, saved.amount)

        // auto_process=true: collapse PENDING → SUCCESS into a single atomic call.
        // Use for CASH / UPI / WALLET where success is known at request time.
        if (request.autoProcess) {
            saved.markAsSuccess()
            val processed = paymentRepository.save(saved)
            if (processed.bill != null) {
                updateBillStatusAfterPayment(processed)
            }
            logger.info("Payment auto-processed to SUCCESS - id: {}", processed.id)
            return paymentMapper.toResponse(processed)
        }

        return paymentMapper.toResponse(saved)
    }

    // ==================== READ OPERATIONS ====================

    @Transactional(readOnly = true)
    override fun getPaymentById(id: Long): PaymentResponse? {
        logger.debug("Fetching payment: {}", id)
        return paymentRepository.findById(id)
            .map { paymentMapper.toResponse(it) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun getPaymentByReferenceNumber(referenceNumber: String): PaymentResponse? {
        logger.debug("Fetching payment by reference: {}", referenceNumber)
        return paymentRepository.findByReferenceNumber(referenceNumber)
            ?.let { paymentMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getAllPayments(pageable: Pageable): Page<PaymentListResponse> {
        logger.debug("Fetching all payments - page: {}", pageable.pageNumber)
        return paymentRepository.findAll(pageable)
            .map { paymentMapper.toListResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getPaymentsByOrder(orderId: Long, pageable: Pageable): Page<PaymentListResponse> {
        logger.debug("Fetching payments for order: {}", orderId)

        if (!orderRepository.existsById(orderId)) {
            logger.error("Order not found: {}", orderId)
            throw AppException.ResourceNotFoundException("Order not found: $orderId")
        }

        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId, pageable)
            .map { paymentMapper.toListResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getPaymentsByBill(billId: Long, pageable: Pageable): Page<PaymentListResponse> {
        logger.debug("Fetching payments for bill: {}", billId)

        if (!billRepository.existsById(billId)) {
            logger.error("Bill not found: {}", billId)
            throw AppException.ResourceNotFoundException("Bill not found: $billId")
        }

        return paymentRepository.findByBillIdOrderByCreatedAtDesc(billId, pageable)
            .map { paymentMapper.toListResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getPaymentsByStatus(status: String, pageable: Pageable): Page<PaymentListResponse> {
        logger.debug("Fetching payments by status: {}", status)

        // Validate status
        if (!isValidPaymentStatus(status)) {
            logger.warn("Invalid payment status: {}", status)
            throw AppException.ValidationException("Invalid payment status: $status")
        }

        return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map { paymentMapper.toListResponse(it) }
    }

    // ==================== UPDATE OPERATIONS ====================

    override fun updatePaymentStatus(id: Long, request: PaymentStatusUpdateRequest): PaymentResponse? {
        logger.debug("Updating payment status - id: {}, status: {}", id, request.status)

        val payment = paymentRepository.findById(id)
            .orElseThrow {
                logger.error("Payment not found: {}", id)
                AppException.ResourceNotFoundException("Payment not found: $id")
            }

        // Validate status
        if (!isValidPaymentStatus(request.status)) {
            logger.warn("Invalid payment status: {}", request.status)
            throw AppException.ValidationException("Invalid payment status: ${request.status}")
        }

        // Validate status transition
        if (!canTransitionStatus(payment.status, request.status)) {
            logger.warn("Invalid status transition - from: {}, to: {}", payment.status, request.status)
            throw AppException.ValidationException(
                "Cannot transition from ${payment.status} to ${request.status}"
            )
        }

        // Update payment
        payment.status = request.status
        payment.transactionId = request.transactionId ?: payment.transactionId
        payment.notes = request.notes ?: payment.notes
        payment.updatedAt = LocalDateTime.now()

        // If payment successful and bill exists, set bill status based on cumulative amount paid
        if (request.status == "SUCCESS" && payment.bill != null) {
            updateBillStatusAfterPayment(payment)
        }

        val updated = paymentRepository.save(payment)
        logger.info("Payment status updated - id: {}, status: {}", id, request.status)
        return paymentMapper.toResponse(updated)
    }

    override fun processPayment(id: Long): PaymentResponse? {
        logger.debug("Processing payment: {}", id)

        val payment = paymentRepository.findById(id)
            .orElseThrow {
                logger.error("Payment not found: {}", id)
                AppException.ResourceNotFoundException("Payment not found: $id")
            }

        if (payment.status != "PENDING") {
            logger.warn("Payment cannot be processed - status: {}", payment.status)
            throw AppException.ValidationException("Only PENDING payments can be processed")
        }

        payment.markAsSuccess()

        // Update associated bill based on cumulative amount paid
        if (payment.bill != null) {
            updateBillStatusAfterPayment(payment)
        }

        val updated = paymentRepository.save(payment)
        logger.info("Payment processed successfully: {}", id)
        return paymentMapper.toResponse(updated)
    }

    override fun refundPayment(id: Long): PaymentResponse? {
        logger.debug("Refunding payment: {}", id)

        val payment = paymentRepository.findById(id)
            .orElseThrow {
                logger.error("Payment not found: {}", id)
                AppException.ResourceNotFoundException("Payment not found: $id")
            }

        if (payment.status != "SUCCESS") {
            logger.warn("Only successful payments can be refunded - status: {}", payment.status)
            throw AppException.ValidationException("Only SUCCESS payments can be refunded")
        }

        payment.markAsRefunded()
        val updated = paymentRepository.save(payment)
        logger.info("Payment refunded: {}", id)
        return paymentMapper.toResponse(updated)
    }

    // ==================== UTILITY METHODS ====================

    @Transactional(readOnly = true)
    override fun isPaymentProcessed(referenceNumber: String): Boolean {
        val payment = paymentRepository.findByReferenceNumber(referenceNumber)
        return payment?.status == "SUCCESS"
    }

    @Transactional(readOnly = true)
    override fun validatePaymentAmount(billId: Long, amount: BigDecimal): Boolean {
        val bill = billRepository.findById(billId).orElse(null) ?: return false
        return amount == bill.totalAmount || (amount > BigDecimal.ZERO && amount <= bill.totalAmount)
    }

    private fun isValidPaymentMethod(method: String): Boolean {
        return method in listOf("CASH", "CARD", "UPI", "WALLET")
    }

    private fun isValidPaymentStatus(status: String): Boolean {
        return status in listOf("PENDING", "SUCCESS", "FAILED", "REFUNDED")
    }

    private fun canTransitionStatus(current: String, new: String): Boolean {
        val validTransitions = mapOf(
            "PENDING" to listOf("SUCCESS", "FAILED"),
            "SUCCESS" to listOf("REFUNDED"),
            "FAILED" to listOf("PENDING"),
            "REFUNDED" to listOf()
        )

        return validTransitions[current]?.contains(new) ?: false
    }

    /**
     * After marking a payment as SUCCESS, compute total amount paid against the bill
     * (previous SUCCESS payments + this payment) and set bill status accordingly:
     *  - total paid >= bill total  →  PAID
     *  - total paid <  bill total  →  PARTIAL
     */
    private fun updateBillStatusAfterPayment(payment: Payment) {
        val bill = payment.bill ?: return
        val billId = bill.id ?: return

        // Sum of all previously confirmed payments for this bill (excluding the current one
        // which is not yet persisted as SUCCESS in the DB)
        val previouslyPaid = paymentRepository.findByBillIdOrderByCreatedAtDesc(billId)
            .filter { it.status == "SUCCESS" && it.id != payment.id }
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.amount }

        val totalPaid = previouslyPaid + payment.amount

        if (totalPaid >= bill.totalAmount) {
            bill.markAsPaid()
            logger.info("Bill {} fully paid — total paid: {}, bill total: {}", billId, totalPaid, bill.totalAmount)
        } else {
            bill.markAsPartial()
            logger.info("Bill {} partially paid — paid so far: {}, bill total: {}", billId, totalPaid, bill.totalAmount)
        }
    }
}

