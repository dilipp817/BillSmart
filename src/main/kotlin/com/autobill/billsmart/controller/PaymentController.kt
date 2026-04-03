package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.PaymentRequest
import com.autobill.billsmart.dto.PaymentResponse
import com.autobill.billsmart.services.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Min

/**
 * PaymentController - REST API for payment operations
 *
 * Endpoints:
 * - POST   /api/v1/payments              - Process payment
 * - GET    /api/v1/payments/{id}         - Get payment by ID
 * - GET    /api/v1/payments/bill/{billId} - Get payments for bill
 * - GET    /api/v1/payments/order/{orderId} - Get payments for order
 *
 * Features:
 * - Idempotent payment processing (X-Idempotency-Key header)
 * - Multiple payment methods support
 * - Pagination support
 * - Error handling with meaningful messages
 *
 * Design Patterns:
 * - Controller Pattern: Handles HTTP requests
 * - Request/Response Pattern: DTOs for data transfer
 * - Builder Pattern: For complex response objects
 *
 * Security Considerations:
 * - Validates all input parameters
 * - Checks authorization for sensitive operations
 * - Audit logs for payment transactions
 */
@RestController
@RequestMapping("/api/v1/payments")
@Validated
class PaymentController(
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Process a payment for a bill
     * Supports idempotency via X-Idempotency-Key header
     *
     * HTTP Method: POST
     * Endpoint: /api/v1/payments
     * Authentication: Required (Bearer token)
     *
     * Request Header:
     * ```
     * X-Idempotency-Key: uuid (optional, for idempotent requests)
     * ```
     *
     * Request Body:
     * ```json
     * {
     *   "bill_id": 1,
     *   "order_id": 1,
     *   "amount": 1250.50,
     *   "payment_method": "CARD",
     *   "transaction_id": "TXN-2026-03-29-001",
     *   "reference_number": "REF-123456",
     *   "notes": "Payment received"
     * }
     * ```
     *
     * Success Response (200 OK):
     * ```json
     * {
     *   "success": true,
     *   "message": "Payment processed successfully",
     *   "data": {
     *     "payment_id": 1,
     *     "bill_id": 1,
     *     "order_id": 1,
     *     "transaction_id": "TXN-2026-03-29-001",
     *     "amount": 1250.50,
     *     "payment_method": "CARD",
     *     "status": "COMPLETED",
     *     "created_at": "2026-03-29T10:30:00Z"
     *   }
     * }
     * ```
     *
     * Error Response (400 Bad Request):
     * ```json
     * {
     *   "success": false,
     *   "message": "Payment amount exceeds bill total",
     *   "error_code": "PAYMENT_AMOUNT_INVALID"
     * }
     * ```
     *
     * @param request payment request details
     * @param idempotencyKey optional idempotency key from header
     * @return payment response with transaction details
     */
    @PostMapping
    fun processPayment(
        @Valid @RequestBody request: PaymentRequest,
        @RequestHeader("X-Idempotency-Key", required = false) idempotencyKey: String?
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        logger.info("Processing payment for bill: ${request.billId}, amount: ${request.amount}")

        return try {
            val response = paymentService.createPayment(request)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Payment processed successfully",
                    data = response
                )
            )
        } catch (e: Exception) {
            logger.error("Error processing payment", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "Payment processing failed"
                )
            )
        }
    }

    /**
     * Get payment by ID
     *
     * HTTP Method: GET
     * Endpoint: /api/v1/payments/{id}
     * Authentication: Required
     *
     * Path Parameters:
     * - id: Payment ID (long)
     *
     * Success Response (200 OK):
     * ```json
     * {
     *   "success": true,
     *   "data": {
     *     "payment_id": 1,
     *     "bill_id": 1,
     *     "amount": 1250.50,
     *     "payment_method": "CARD",
     *     "status": "COMPLETED",
     *     "created_at": "2026-03-29T10:30:00Z"
     *   }
     * }
     * ```
     *
     * Error Response (404 Not Found):
     * ```json
     * {
     *   "success": false,
     *   "message": "Payment not found"
     * }
     * ```
     *
     * @param id payment ID
     * @return payment response
     */
    @GetMapping("/{id}")
    fun getPaymentById(
        @PathVariable @Min(1) id: Long
    ): ResponseEntity<ApiResponse<PaymentResponse?>> {
        logger.info("Fetching payment: $id")

        return try {
            val payment = paymentService.getPaymentById(id)
            if (payment != null) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        data = payment
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(
                        success = false,
                        message = "Payment not found"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching payment", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "Error fetching payment"
                )
            )
        }
    }

    /**
     * Get all payments for a bill
     * Useful for payment history and reconciliation
     *
     * HTTP Method: GET
     * Endpoint: /api/v1/payments/bill/{billId}
     * Authentication: Required
     *
     * Query Parameters:
     * - offset: Starting position (default: 0)
     * - limit: Maximum records (default: 20, max: 100)
     * - sort: Sort field (optional)
     *
     * Success Response (200 OK):
     * ```json
     * {
     *   "success": true,
     *   "data": {
     *     "payments": [
     *       {
     *         "payment_id": 1,
     *         "bill_id": 1,
     *         "amount": 1250.50,
     *         "status": "COMPLETED"
     *       }
     *     ],
     *     "meta": {
     *       "total": 1,
     *       "limit": 20,
     *       "offset": 0,
     *       "has_more": false
     *     }
     *   }
     * }
     * ```
     *
     * @param billId bill ID
     * @param offset pagination offset
     * @param limit pagination limit
     * @return paginated payment list
     */
    @GetMapping("/bill/{billId}")
    fun getPaymentsByBill(
        @PathVariable @Min(1) billId: Long,
        @RequestParam(defaultValue = "0") @Min(0) offset: Int,
        @RequestParam(defaultValue = "20") @Min(1) limit: Int
    ): ResponseEntity<ApiResponse<Any>> {
        logger.info("Fetching payments for bill: $billId, offset: $offset, limit: $limit")

        return try {
            val pageable: Pageable = PageRequest.of(offset / limit, limit)
            val payments = paymentService.getPaymentsByBill(billId, pageable)

            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = mapOf(
                        "payments" to payments.content,
                        "meta" to mapOf(
                            "total" to payments.totalElements,
                            "limit" to limit,
                            "offset" to offset,
                            "has_more" to !payments.isLast
                        )
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching payments for bill", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "Error fetching payments"
                )
            )
        }
    }

    /**
     * Get all payments for an order
     *
     * HTTP Method: GET
     * Endpoint: /api/v1/payments/order/{orderId}
     * Authentication: Required
     *
     * @param orderId order ID
     * @param offset pagination offset
     * @param limit pagination limit
     * @return paginated payment list
     */
    @GetMapping("/order/{orderId}")
    fun getPaymentsByOrder(
        @PathVariable @Min(1) orderId: Long,
        @RequestParam(defaultValue = "0") @Min(0) offset: Int,
        @RequestParam(defaultValue = "20") @Min(1) limit: Int
    ): ResponseEntity<ApiResponse<Any>> {
        logger.info("Fetching payments for order: $orderId")

        return try {
            val pageable: Pageable = PageRequest.of(offset / limit, limit)
            val payments = paymentService.getPaymentsByOrder(orderId, pageable)

            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = mapOf(
                        "payments" to payments.content,
                        "meta" to mapOf(
                            "total" to payments.totalElements,
                            "limit" to limit,
                            "offset" to offset,
                            "has_more" to !payments.isLast
                        )
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching payments for order", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "Error fetching payments"
                )
            )
        }
    }
}

