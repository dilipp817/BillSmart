package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.TableAvailabilityResponse
import com.autobill.billsmart.dto.TableRequest
import com.autobill.billsmart.dto.TableResponse
import com.autobill.billsmart.dto.TableStatusUpdateRequest
import com.autobill.billsmart.dto.TablesListResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.model.enums.TableStatus
import com.autobill.billsmart.security.TenantUtils
import com.autobill.billsmart.services.TableService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * TablesController - REST API endpoints for table management
 *
 * Error handling is centralised in [GlobalExceptionHandler] — controllers simply
 * let AppException propagate and the @RestControllerAdvice maps it to the correct
 * HTTP status automatically.
 */
@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/tables")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class TablesController(
    private val tableService: TableService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE ====================

    @PostMapping
    fun createTable(
        @PathVariable restaurantId: Long,
        @Valid @RequestBody request: TableRequest
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("POST: Create table for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)
        val response = tableService.createTable(restaurantId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(data = response, message = "Table created successfully"))
    }

    // ==================== READ ====================

    @GetMapping
    fun getTablesByRestaurant(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<TablesListResponse>> {
        logger.info("GET: Fetch tables for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)
        val tables = tableService.getTablesByRestaurant(restaurantId)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = TablesListResponse(tables = tables, total = tables.size.toLong()),
                message = "Tables retrieved successfully"
            )
        )
    }

    @GetMapping("/{id}")
    fun getTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("GET: Fetch table - ID: {}", id)
        TenantUtils.assertTenantAccess(restaurantId)
        val table = tableService.getTable(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
        // Return 404 (not 403) to avoid leaking cross-tenant IDs
        if (table.restaurantId != restaurantId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
        }
        return ResponseEntity.ok(ApiResponse.success(data = table, message = "Table retrieved successfully"))
    }

    @GetMapping("/available")
    fun getAvailableTables(
        @PathVariable restaurantId: Long,
        @RequestParam(required = false) capacity: Int?
    ): ResponseEntity<ApiResponse<List<TableAvailabilityResponse>>> {
        logger.info("GET: Fetch available tables - restaurant: {}, capacity: {}", restaurantId, capacity)
        TenantUtils.assertTenantAccess(restaurantId)
        val tables = tableService.getAvailableTables(restaurantId, capacity)
        return ResponseEntity.ok(
            ApiResponse.success(data = tables, message = "Available tables retrieved successfully")
        )
    }

    @GetMapping("/occupied")
    fun getOccupiedTables(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<List<TableResponse>>> {
        logger.info("GET: Fetch occupied tables - restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)
        val tables = tableService.getOccupiedTables(restaurantId)
        return ResponseEntity.ok(ApiResponse.success(data = tables, message = "Occupied tables retrieved successfully"))
    }

    @GetMapping("/status/{status}")
    fun getTablesByStatus(
        @PathVariable restaurantId: Long,
        @PathVariable status: TableStatus
    ): ResponseEntity<ApiResponse<List<TableResponse>>> {
        logger.info("GET: Fetch tables by status - status: {}", status)
        TenantUtils.assertTenantAccess(restaurantId)
        val tables = tableService.getTablesByStatus(restaurantId, status)
        return ResponseEntity.ok(ApiResponse.success(data = tables, message = "Tables retrieved successfully"))
    }

    @GetMapping("/count/available")
    fun countAvailableTables(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<Long>> {
        logger.info("GET: Count available tables - restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)
        val count = tableService.countAvailableTables(restaurantId)
        return ResponseEntity.ok(
            ApiResponse.success(data = count, message = "Available tables count retrieved successfully")
        )
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    fun updateTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: TableRequest
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("PUT: Update table - ID: {}", id)
        TenantUtils.assertTenantAccess(restaurantId)
        requireTableBelongsToRestaurant(restaurantId, id)
        // updateTable throws ResourceNotFoundException internally if the table is missing;
        // the null branch below is kept only as a safety net.
        val response = tableService.updateTable(id, request)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
        return ResponseEntity.ok(ApiResponse.success(data = response, message = "Table updated successfully"))
    }

    @PatchMapping("/{id}/status")
    fun updateTableStatus(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: TableStatusUpdateRequest
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("PATCH: Update table status - ID: {}, newStatus: {}", id, request.newStatus)
        TenantUtils.assertTenantAccess(restaurantId)
        requireTableBelongsToRestaurant(restaurantId, id)
        val response = tableService.updateTableStatus(id, request.newStatus)
        return ResponseEntity.ok(ApiResponse.success(data = response, message = "Table status updated successfully"))
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    fun deleteTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("DELETE: Delete table - ID: {}", id)
        TenantUtils.assertTenantAccess(restaurantId)
        requireTableBelongsToRestaurant(restaurantId, id)
        val deleted = tableService.deleteTable(id)
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
        }
        return ResponseEntity.ok(ApiResponse.success(data = "Table deleted successfully", message = "Table removed"))
    }

    // ==================== HELPERS ====================

    /**
     * Tenant isolation guard — ensures the table belongs to the restaurant in the URL path.
     * Returns 404 (not 403) to avoid revealing that the table exists in a different tenant.
     * Delegates to [TenantUtils.assertResourceOwnership] to avoid duplicating that logic.
     */
    private fun requireTableBelongsToRestaurant(restaurantId: Long, tableId: Long) {
        val table = tableService.getTable(tableId)
            ?: throw AppException.ResourceNotFoundException("Table not found")
        // assertResourceOwnership compares the resource's restaurantId against the JWT claim;
        // we additionally verify it matches the path variable to catch any mismatch.
        if (table.restaurantId != restaurantId)
            throw AppException.ResourceNotFoundException("Table not found")
    }
}

