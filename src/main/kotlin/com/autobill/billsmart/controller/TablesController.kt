package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.TableAvailabilityResponse
import com.autobill.billsmart.dto.TableRequest
import com.autobill.billsmart.dto.TableResponse
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
 * REST Principles:
 * - Resource-Oriented Design: Tables as resources
 * - HTTP Methods: GET (retrieve), POST (create), PUT (update), DELETE (delete)
 * - Status Codes: Appropriate HTTP status codes for responses
 * - Stateless: Each request contains all necessary information
 *
 * Design Patterns:
 * - Controller Layer: Handles HTTP communication
 * - DTO Pattern: Uses DTOs for request/response
 * - Dependency Injection: Services injected via constructor
 *
 * Performance:
 * - Validation at controller level (@Valid)
 * - Error handling with custom exceptions
 * - Logging for debugging and monitoring
 */
@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/tables")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class TablesController(
    private val tableService: TableService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE OPERATIONS ====================

    /**
     * Create a new table
     *
     * @param restaurantId restaurant ID
     * @param request table creation request
     * @return created table response with 201 status
     *
     * POST /api/v1/restaurants/{restaurantId}/tables
     */
    @PostMapping
    fun createTable(
        @PathVariable restaurantId: Long,
        @Valid @RequestBody request: TableRequest
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("POST: Create table for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val response = tableService.createTable(restaurantId, request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data = response, message = "Table created successfully"))
        } catch (e: AppException) {
            logger.error("Error creating table: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Get all tables for restaurant
     *
     * @param restaurantId restaurant ID
     * @return list of tables
     *
     * GET /api/v1/restaurants/{restaurantId}/tables
     */
    @GetMapping
    fun getTablesByRestaurant(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<TablesListResponse>> {
        logger.info("GET: Fetch tables for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val tables = tableService.getTablesByRestaurant(restaurantId)
            ResponseEntity.ok(ApiResponse.success(data = TablesListResponse(tables = tables, total = tables.size.toLong()), message = "Tables retrieved successfully"))
        } catch (e: AppException) {
            logger.error("Error fetching tables: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Get table by ID
     *
     * @param restaurantId restaurant ID
     * @param id table ID
     * @return table details
     *
     * GET /api/v1/restaurants/{restaurantId}/tables/{id}
     */
    @GetMapping("/{id}")
    fun getTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("GET: Fetch table - ID: {}", id)
        TenantUtils.assertTenantAccess(restaurantId)

        val table = tableService.getTable(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))

        // Ownership: table must belong to the restaurant in the URL
        if (table.restaurantId != restaurantId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
        }

        return ResponseEntity.ok(ApiResponse.success(data = table, message = "Table retrieved successfully"))
    }

    /**
     * Get available tables with optional capacity filter
     *
     * @param restaurantId restaurant ID
     * @param capacity optional minimum capacity
     * @return list of available tables
     *
     * GET /api/v1/restaurants/{restaurantId}/tables/available?capacity=4
     */
    @GetMapping("/available")
    fun getAvailableTables(
        @PathVariable restaurantId: Long,
        @RequestParam(required = false) capacity: Int?
    ): ResponseEntity<ApiResponse<List<TableAvailabilityResponse>>> {
        logger.info("GET: Fetch available tables - restaurant: {}, capacity: {}", restaurantId, capacity)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val tables = tableService.getAvailableTables(restaurantId, capacity)
            ResponseEntity.ok(ApiResponse.success(data = tables, message = "Available tables retrieved successfully"))
        } catch (e: AppException) {
            logger.error("Error fetching available tables: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Get occupied tables
     *
     * @param restaurantId restaurant ID
     * @return list of occupied tables
     *
     * GET /api/v1/restaurants/{restaurantId}/tables/occupied
     */
    @GetMapping("/occupied")
    fun getOccupiedTables(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<List<TableResponse>>> {
        logger.info("GET: Fetch occupied tables - restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val tables = tableService.getOccupiedTables(restaurantId)
            ResponseEntity.ok(ApiResponse.success(data = tables, message = "Occupied tables retrieved successfully"))
        } catch (e: AppException) {
            logger.error("Error fetching occupied tables: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Get tables by status
     *
     * @param restaurantId restaurant ID
     * @param status table status filter
     * @return list of tables with given status
     *
     * GET /api/v1/restaurants/{restaurantId}/tables/status/{status}
     */
    @GetMapping("/status/{status}")
    fun getTablesByStatus(
        @PathVariable restaurantId: Long,
        @PathVariable status: TableStatus
    ): ResponseEntity<ApiResponse<List<TableResponse>>> {
        logger.info("GET: Fetch tables by status - status: {}", status)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val tables = tableService.getTablesByStatus(restaurantId, status)
            ResponseEntity.ok(ApiResponse.success(data = tables, message = "Tables retrieved successfully"))
        } catch (e: AppException) {
            logger.error("Error fetching tables by status: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Count available tables
     *
     * @param restaurantId restaurant ID
     * @return count of available tables
     *
     * GET /api/v1/restaurants/{restaurantId}/tables/count/available
     */
    @GetMapping("/count/available")
    fun countAvailableTables(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<Long>> {
        logger.info("GET: Count available tables - restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            val count = tableService.countAvailableTables(restaurantId)
            ResponseEntity.ok(ApiResponse.success(data = count, message = "Available tables count retrieved successfully"))
        } catch (e: AppException) {
            logger.error("Error counting available tables: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update table details
     *
     * @param restaurantId restaurant ID
     * @param id table ID
     * @param request update request
     * @return updated table
     *
     * PUT /api/v1/restaurants/{restaurantId}/tables/{id}
     */
    @PutMapping("/{id}")
    fun updateTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: TableRequest
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("PUT: Update table - ID: {}", id)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            // Ownership check: verify table belongs to this restaurant before updating
            val existing = tableService.getTable(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            if (existing.restaurantId != restaurantId) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            }

            val response = tableService.updateTable(id, request)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            ResponseEntity.ok(ApiResponse.success(data = response, message = "Table updated successfully"))
        } catch (e: AppException) {
            logger.error("Error updating table: {}", e.message)
            handleAppException(e)
        }
    }

    /**
     * Update table status
     *
     * @param restaurantId restaurant ID
     * @param id table ID
     * @param newStatus new status
     * @return updated table
     *
     * PATCH /api/v1/restaurants/{restaurantId}/tables/{id}/status
     */
    @PatchMapping("/{id}/status")
    fun updateTableStatus(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @RequestParam(name = "new_status") newStatus: TableStatus
    ): ResponseEntity<ApiResponse<TableResponse>> {
        logger.info("PATCH: Update table status - ID: {}, newStatus: {}", id, newStatus)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            // Ownership check
            val existing = tableService.getTable(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            if (existing.restaurantId != restaurantId) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            }

            val response = tableService.updateTableStatus(id, newStatus)
            ResponseEntity.ok(ApiResponse.success(data = response, message = "Table status updated successfully"))
        } catch (e: AppException) {
            logger.error("Error updating table status: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete table
     *
     * @param restaurantId restaurant ID
     * @param id table ID
     * @return success message
     *
     * DELETE /api/v1/restaurants/{restaurantId}/tables/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("DELETE: Delete table - ID: {}", id)
        TenantUtils.assertTenantAccess(restaurantId)

        return try {
            // Ownership check
            val existing = tableService.getTable(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            if (existing.restaurantId != restaurantId) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            }

            val deleted = tableService.deleteTable(id)
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(code = "NOT_FOUND", message = "Table not found"))
            }
            ResponseEntity.ok(ApiResponse.success(data = "Table deleted successfully", message = "Table removed"))
        } catch (e: AppException) {
            logger.error("Error deleting table: {}", e.message)
            handleAppException(e)
        }
    }

    // ==================== EXCEPTION HANDLING ====================

    /**
     * Handle AppException and convert to appropriate HTTP response
     */
    private fun <T> handleAppException(e: AppException): ResponseEntity<ApiResponse<T>> {
        return when (e) {
            is AppException.ResourceNotFoundException -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.message ?: "Resource not found"))
            is AppException.ValidationException -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", e.message ?: "Validation failed"))
            is AppException.ConflictException -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", e.message ?: "Conflict occurred"))
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Internal server error"))
        }
    }
}

