package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.services.BillService
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
 * BillsController - REST API endpoints for bill management
 *
 * Endpoints:
 * - POST   /api/v1/bills              - Create bill
 * - GET    /api/v1/bills              - List all bills (paginated)
 * - GET    /api/v1/bills/{id}         - Get bill by ID
 * - PUT    /api/v1/bills/{id}         - Update bill
 * - DELETE /api/v1/bills/{id}         - Delete bill
 * - PATCH  /api/v1/bills/{id}/paid    - Mark as paid
 * - PATCH  /api/v1/bills/{id}/cancel  - Cancel bill
 * - POST   /api/v1/bills/{id}/items   - Add items
 * - DELETE /api/v1/bills/{id}/items/{itemId} - Remove item
 */
@RestController
@RequestMapping("/api/v1/bills")
class BillsController(
    private val billService: BillService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * POST /api/v1/bills - Create a new bill
     */
    @PostMapping
    fun createBill(@Valid @RequestBody request: BillRequest): ResponseEntity<BillResponse> {
        logger.info("Creating bill - billNumber: {}", request.billNumber)
        val response = billService.createBill(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * GET /api/v1/bills - Get all bills (paginated)
     */
    @GetMapping
    fun getAllBills(
        @RequestParam(required = false) status: String?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<BillListResponse>> {
        logger.debug("Fetching bills - status: {}, page: {}", status, pageable.pageNumber)

        val response = if (status != null) {
            billService.getBillsByStatus(status, pageable)
        } else {
            Page.empty(pageable) // Implement getAllBills in service if needed
        }

        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/v1/bills/{id} - Get bill by ID
     */
    @GetMapping("/{id}")
    fun getBillById(@PathVariable id: Long): ResponseEntity<BillResponse> {
        logger.info("Fetching bill: {}", id)
        val response = billService.getBillById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/v1/bills/number/{billNumber} - Get bill by number
     */
    @GetMapping("/number/{billNumber}")
    fun getBillByNumber(@PathVariable billNumber: String): ResponseEntity<BillResponse> {
        logger.info("Fetching bill by number: {}", billNumber)
        val response = billService.getBillByNumber(billNumber)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * PUT /api/v1/bills/{id} - Update bill
     */
    @PutMapping("/{id}")
    fun updateBill(
        @PathVariable id: Long,
        @Valid @RequestBody request: BillRequest
    ): ResponseEntity<BillResponse> {
        logger.info("Updating bill: {}", id)
        val response = billService.updateBill(id, request)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * PATCH /api/v1/bills/{id}/paid - Mark bill as paid
     */
    @PatchMapping("/{id}/paid")
    fun markBillAsPaid(@PathVariable id: Long): ResponseEntity<BillResponse> {
        logger.info("Marking bill as paid: {}", id)
        val response = billService.markBillAsPaid(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * PATCH /api/v1/bills/{id}/cancel - Cancel bill
     */
    @PatchMapping("/{id}/cancel")
    fun cancelBill(@PathVariable id: Long): ResponseEntity<BillResponse> {
        logger.info("Cancelling bill: {}", id)
        val response = billService.cancelBill(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * POST /api/v1/bills/{id}/items - Add items to bill
     */
    @PostMapping("/{id}/items")
    fun addBillItems(
        @PathVariable id: Long,
        @Valid @RequestBody items: List<BillItemRequest>
    ): ResponseEntity<BillResponse> {
        logger.info("Adding {} items to bill: {}", items.size, id)
        val response = billService.addBillItems(id, items)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * DELETE /api/v1/bills/{id}/items/{itemId} - Remove item from bill
     */
    @DeleteMapping("/{id}/items/{itemId}")
    fun removeBillItem(
        @PathVariable id: Long,
        @PathVariable itemId: Long
    ): ResponseEntity<BillResponse> {
        logger.info("Removing item {} from bill: {}", itemId, id)
        val response = billService.removeBillItem(id, itemId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    /**
     * DELETE /api/v1/bills/{id} - Delete bill
     */
    @DeleteMapping("/{id}")
    fun deleteBill(@PathVariable id: Long): ResponseEntity<Void> {
        logger.info("Deleting bill: {}", id)
        val deleted = billService.deleteBill(id)
        return if (deleted) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }
}

