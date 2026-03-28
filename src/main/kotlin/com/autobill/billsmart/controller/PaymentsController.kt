package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.services.PaymentService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * PaymentsController - REST API endpoints for payment management
 *
 * Endpoints:
 * - POST   /api/v1/payments              - Create payment
 * - GET    /api/v1/payments              - List all payments (paginated)
 * - GET    /api/v1/payments/{id}         - Get payment by ID
 * - PATCH  /api/v1/payments/{id}/status  - Update payment status
 * - GET    /api/v1/payments/order/{orderId} - Get payments for order
 * - POST   /api/v1/payments/{id}/process - Process payment
 * - POST   /api/v1/payments/{id}/refund  - Refund payment
 */
@RestController
@RequestMapping("/api/v1/payments")
class PaymentsController(
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * POST /api/v1/payments - Create a new payment
     */
    @PostMapping
    fun createPayment(@Valid @RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        logger.info("Creating payment - orderId: {}, amount: {}, method: {}",
            request.orderId, request.amount, request.paymentMethod)
        val response = paymentService.createPayment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * GET /api/v1/payments - Get all payments (paginated)
     */
    @GetMapping
    fun getAllPayments(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<PaymentListResponse>> {
        logger.debug("Fetching all payments - page: {}", pageable.pageNumber)
        val response = paymentService.getAllPayments(pageable)
        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/v1/payments/{id} - Get payment by ID
     */
    @GetMapping("/{id}")
    fun getPaymentById(@PathVariable id: Long): ResponseEntity<PaymentResponse> {
        logger.info("Fetching payment: {}", id)
        val response = paymentService.getPaymentById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/v1/payments/reference/{referenceNumber} - Get payment by reference
     */
    @GetMapping("/reference/{referenceNumber}")
    fun getPaymentByReference(@PathVariable referenceNumber: String): ResponseEntity<PaymentResponse> {
        logger.info("Fetching payment by reference: {}", referenceNumber)
        val response = paymentService.getPaymentByReferenceNumber(referenceNumber)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/v1/payments/order/{orderId} - Get payments for order (paginated)
     */
    @GetMapping("/order/{orderId}")
    fun getPaymentsByOrder(
        @PathVariable orderId: Long,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<PaymentListResponse>> {
        logger.info("Fetching payments for order: {}", orderId)
        val response = paymentService.getPaymentsByOrder(orderId, pageable)
        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/v1/payments/status/{status} - Get payments by status (paginated)
     */
    @GetMapping("/status/{status}")
    fun getPaymentsByStatus(
        @PathVariable status: String,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<PaymentListResponse>> {
        logger.info("Fetching payments by status: {}", status)
        val response = paymentService.getPaymentsByStatus(status, pageable)
        return ResponseEntity.ok(response)
    }

    /**
     * PATCH /api/v1/payments/{id}/status - Update payment status
     */
    @PatchMapping("/{id}/status")
    fun updatePaymentStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: PaymentStatusUpdateRequest
    ): ResponseEntity<PaymentResponse> {
        logger.info("Updating payment status - id: {}, status: {}", id, request.status)
        val response = paymentService.updatePaymentStatus(id, request)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * POST /api/v1/payments/{id}/process - Process payment (mark as success)
     */
    @PostMapping("/{id}/process")
    fun processPayment(@PathVariable id: Long): ResponseEntity<PaymentResponse> {
        logger.info("Processing payment: {}", id)
        val response = paymentService.processPayment(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * POST /api/v1/payments/{id}/refund - Refund payment
     */
    @PostMapping("/{id}/refund")
    fun refundPayment(@PathVariable id: Long): ResponseEntity<PaymentResponse> {
        logger.info("Refunding payment: {}", id)
        val response = paymentService.refundPayment(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }
}

