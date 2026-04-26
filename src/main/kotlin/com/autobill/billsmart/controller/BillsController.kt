package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.security.TenantUtils
import com.autobill.billsmart.services.BillService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
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
    fun createBill(@Valid @RequestBody request: BillRequest): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Creating bill for order: {}", request.orderId)
        // Verify the restaurantId in the request body belongs to the caller
        TenantUtils.assertTenantAccess(request.restaurantId)
        val response = billService.createBill(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Bill created successfully"))
    }

    /** GET /api/v1/bills - Get all bills (paginated) — scoped to the caller's restaurant */
    @GetMapping
    fun getAllBills(
        @RequestParam(required = false) status: String?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<List<BillListResponse>>> {
        logger.debug("Fetching bills - status: {}, page: {}", status, pageable.pageNumber)

        // Scope results to the caller's restaurant; super_admin (null) sees all
        val jwtRestaurantId = TenantUtils.getJwtRestaurantId()

        val page = when {
            jwtRestaurantId != null -> billService.getBillsByRestaurant(jwtRestaurantId, pageable)
            status != null          -> billService.getBillsByStatus(status, pageable)
            else                    -> org.springframework.data.domain.Page.empty(pageable)
        }
        return ResponseEntity.ok(ApiResponse.success(page.content, "Bills retrieved successfully"))
    }

    /**
     * GET /api/v1/bills/{id} - Get bill by ID
     */
    @GetMapping("/{id}")
    fun getBillById(@PathVariable id: Long): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Fetching bill: {}", id)
        val response = billService.getBillById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        // Tenant ownership check
        TenantUtils.assertResourceOwnership(response.restaurantId)
        return ResponseEntity.ok(ApiResponse.success(response, "Bill retrieved successfully"))
    }

    /**
     * GET /api/v1/bills/number/{billNumber} - Get bill by number
     */
    @GetMapping("/number/{billNumber}")
    fun getBillByNumber(@PathVariable billNumber: String): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Fetching bill by number: {}", billNumber)
        val response = billService.getBillByNumber(billNumber)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found: $billNumber"))
        // Tenant ownership check
        TenantUtils.assertResourceOwnership(response.restaurantId)
        return ResponseEntity.ok(ApiResponse.success(response, "Bill retrieved successfully"))
    }

    /**
     * PATCH /api/v1/bills/{id}/paid - Mark bill as paid
     */
    @PatchMapping("/{id}/paid")
    fun markBillAsPaid(@PathVariable id: Long): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Marking bill as paid: {}", id)
        // Ownership check before mutation
        val existing = billService.getBillById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        TenantUtils.assertResourceOwnership(existing.restaurantId)
        val response = billService.markBillAsPaid(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        return ResponseEntity.ok(ApiResponse.success(response, "Bill marked as paid"))
    }

    /**
     * PATCH /api/v1/bills/{id}/cancel - Cancel bill
     */
    @PatchMapping("/{id}/cancel")
    fun cancelBill(@PathVariable id: Long): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Cancelling bill: {}", id)
        // Ownership check before mutation
        val existing = billService.getBillById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        TenantUtils.assertResourceOwnership(existing.restaurantId)
        val response = billService.cancelBill(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        return ResponseEntity.ok(ApiResponse.success(response, "Bill cancelled successfully"))
    }

    /**
     * POST /api/v1/bills/{id}/items - Add items to bill
     */
    @PostMapping("/{id}/items")
    fun addBillItems(
        @PathVariable id: Long,
        @Valid @RequestBody items: List<BillItemRequest>
    ): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Adding {} items to bill: {}", items.size, id)
        // Ownership check before mutation
        val existing = billService.getBillById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        TenantUtils.assertResourceOwnership(existing.restaurantId)
        val response = billService.addBillItems(id, items)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Items added to bill successfully"))
    }

    /**
     * DELETE /api/v1/bills/{id}/items/{itemId} - Remove item from bill
     */
    @DeleteMapping("/{id}/items/{itemId}")
    fun removeBillItem(
        @PathVariable id: Long,
        @PathVariable itemId: Long
    ): ResponseEntity<ApiResponse<BillResponse>> {
        logger.info("Removing item {} from bill: {}", itemId, id)
        // Ownership check before mutation
        val existing = billService.getBillById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        TenantUtils.assertResourceOwnership(existing.restaurantId)
        val response = billService.removeBillItem(id, itemId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Bill not found with ID: $id"))
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from bill successfully"))
    }
}


