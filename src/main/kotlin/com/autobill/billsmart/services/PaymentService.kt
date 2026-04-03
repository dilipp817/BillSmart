package com.autobill.billsmart.services

import com.autobill.billsmart.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * PaymentService - Interface for payment operations
 */
interface PaymentService {

    /**
     * Create payment
     *
     * @param request payment creation request
     * @return created payment response
     */
    fun createPayment(request: PaymentRequest): PaymentResponse

    /**
     * Get payment by ID
     *
     * @param id payment ID
     * @return payment response or null if not found
     */
    fun getPaymentById(id: Long): PaymentResponse?

    /**
     * Get payment by reference number
     *
     * @param referenceNumber payment reference
     * @return payment response or null if not found
     */
    fun getPaymentByReferenceNumber(referenceNumber: String): PaymentResponse?

    /**
     * Get all payments (paginated)
     *
     * @param pageable pagination info
     * @return page of payments
     */
    fun getAllPayments(pageable: Pageable): Page<PaymentListResponse>

    /**
     * Get payments by order (paginated)
     *
     * @param orderId order ID
     * @param pageable pagination info
     * @return page of payments
     */
    fun getPaymentsByOrder(orderId: Long, pageable: Pageable): Page<PaymentListResponse>

    /**
     * Get payments by bill (paginated)
     *
     * @param billId bill ID
     * @param pageable pagination info
     * @return page of payments
     */
    fun getPaymentsByBill(billId: Long, pageable: Pageable): Page<PaymentListResponse>

    /**
     * Get payments by status (paginated)
     *
     * @param status payment status
     * @param pageable pagination info
     * @return page of payments
     */
    fun getPaymentsByStatus(status: String, pageable: Pageable): Page<PaymentListResponse>

    /**
     * Update payment status
     *
     * @param id payment ID
     * @param request status update request
     * @return updated payment response
     */
    fun updatePaymentStatus(id: Long, request: PaymentStatusUpdateRequest): PaymentResponse?

    /**
     * Process payment (mark as success)
     *
     * @param id payment ID
     * @return updated payment response
     */
    fun processPayment(id: Long): PaymentResponse?

    /**
     * Refund payment
     *
     * @param id payment ID
     * @return updated payment response
     */
    fun refundPayment(id: Long): PaymentResponse?

    /**
     * Verify payment idempotency
     *
     * @param referenceNumber payment reference
     * @return true if payment already processed
     */
    fun isPaymentProcessed(referenceNumber: String): Boolean

    /**
     * Validate payment amount against bill
     *
     * @param billId bill ID
     * @param amount payment amount
     * @return true if amount matches or is valid partial payment
     */
    fun validatePaymentAmount(billId: Long, amount: java.math.BigDecimal): Boolean
}

