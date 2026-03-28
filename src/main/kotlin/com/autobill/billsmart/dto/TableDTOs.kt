package com.autobill.billsmart.dto

import com.autobill.billsmart.model.enums.TableStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

/**
 * TableRequest DTO - Request payload for creating/updating tables
 *
 * Design Principles:
 * - Separation of Concerns: Decouples API from Entity
 * - Immutability: Data class with val fields
 * - Validation: Bean Validation annotations for input validation
 */
data class TableRequest(
    @field:NotBlank(message = "Table number cannot be blank")
    val tableNumber: String,

    @field:Positive(message = "Capacity must be greater than 0")
    val capacity: Int,

    val status: TableStatus? = TableStatus.AVAILABLE
)

/**
 * TableResponse DTO - Response payload for table operations
 *
 * Design Principles:
 * - Read-Only: Immutable response object
 * - Complete Data: Contains all necessary information
 * - Audit Info: Includes creation and update timestamps
 */
data class TableResponse(
    val id: Long,
    val restaurantId: Long,
    val tableNumber: String,
    val capacity: Int,
    val status: TableStatus,
    val currentOrderId: Long? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val version: Long = 0
)

/**
 * TableStatusUpdateRequest DTO - Request for status updates
 *
 * Single Responsibility: Only handles status transitions
 */
data class TableStatusUpdateRequest(
    val newStatus: TableStatus
)

/**
 * TableAvailabilityResponse DTO - Response for availability queries
 *
 * Optimized for client needs: Minimal data for availability checks
 */
data class TableAvailabilityResponse(
    val id: Long,
    val tableNumber: String,
    val capacity: Int,
    val status: TableStatus
)

/**
 * TablesListResponse DTO - Response for list operations
 *
 * Pagination Support: Includes metadata for list operations
 */
data class TablesListResponse(
    val tables: List<TableResponse>,
    val total: Long,
    val status: String = "success"
)

