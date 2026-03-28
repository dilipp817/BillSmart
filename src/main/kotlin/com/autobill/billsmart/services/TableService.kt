package com.autobill.billsmart.services

import com.autobill.billsmart.dto.TableAvailabilityResponse
import com.autobill.billsmart.dto.TableRequest
import com.autobill.billsmart.dto.TableResponse
import com.autobill.billsmart.model.enums.TableStatus

/**
 * TableService Interface - Service contract for table operations
 *
 * SOLID Principles:
 * - Interface Segregation: Focused service contract
 * - Dependency Inversion: Depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Business logic for table management
 * - Validation and error handling
 * - Transaction management
 */
interface TableService {

    // CREATE Operations
    fun createTable(restaurantId: Long, request: TableRequest): TableResponse

    // READ Operations
    fun getTable(id: Long): TableResponse?
    fun getTablesByRestaurant(restaurantId: Long): List<TableResponse>
    fun getAvailableTables(restaurantId: Long, minCapacity: Int? = null): List<TableAvailabilityResponse>
    fun getTablesByStatus(restaurantId: Long, status: TableStatus): List<TableResponse>
    fun getOccupiedTables(restaurantId: Long): List<TableResponse>
    fun countAvailableTables(restaurantId: Long): Long

    // UPDATE Operations
    fun updateTable(id: Long, request: TableRequest): TableResponse?
    fun updateTableStatus(id: Long, newStatus: TableStatus): TableResponse
    fun assignOrderToTable(tableId: Long, orderId: Long): TableResponse
    fun releaseTableFromOrder(tableId: Long): TableResponse

    // DELETE Operations
    fun deleteTable(id: Long): Boolean

    // UTILITY Operations
    fun validateTableNumber(restaurantId: Long, tableNumber: String): Boolean
    fun canTableAcceptOrder(tableId: Long): Boolean
}

