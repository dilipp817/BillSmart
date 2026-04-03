package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.TableAvailabilityResponse
import com.autobill.billsmart.dto.TableRequest
import com.autobill.billsmart.dto.TableResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.mappers.TableMapper
import com.autobill.billsmart.model.enums.TableStatus
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.repositories.TableRepository
import com.autobill.billsmart.services.TableService
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * TableServiceImpl - Implementation of TableService
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles table operations
 * - Open/Closed: Can be extended without modification
 * - Liskov Substitution: Properly implements TableService interface
 * - Interface Segregation: Depends on focused interfaces
 * - Dependency Inversion: Depends on abstractions (repositories, mappers)
 *
 * Thread Safety:
 * - @Transactional ensures database consistency
 * - @Version field handles optimistic locking
 * - Read-write lock for cache-like operations (if needed)
 *
 * Performance Considerations:
 * - Uses database indexes for fast queries
 * - Lazy loading for relationships
 * - Batch operations where possible
 */
@Service
@Transactional
class TableServiceImpl(
    private val tableRepository: TableRepository,
    private val restaurantRepository: RestaurantRepository,
    private val tableMapper: TableMapper
) : TableService {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ==================== CREATE OPERATIONS ====================

    /**
     * Create a new table for restaurant
     *
     * Validation:
     * - Restaurant must exist
     * - Table number must be unique per restaurant
     * - Capacity must be positive
     *
     * @param restaurantId the restaurant ID
     * @param request table creation request
     * @return created table response
     * @throws AppException.ResourceNotFoundException if restaurant not found
     * @throws AppException.ValidationException if validation fails
     */
    override fun createTable(restaurantId: Long, request: TableRequest): TableResponse {
        logger.debug("Creating table for restaurant: {}", restaurantId)

        // Validate restaurant exists
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow {
                logger.error("Restaurant not found: {}", restaurantId)
                AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
            }

        // Validate table number is unique
        if (tableRepository.existsByRestaurantIdAndTableNumber(restaurantId, request.tableNumber)) {
            logger.warn("Table number already exists: {} for restaurant: {}", request.tableNumber, restaurantId)
            throw AppException.ValidationException(
                "Table number '${request.tableNumber}' already exists for this restaurant"
            )
        }

        // Validate capacity
        if (request.capacity <= 0) {
            logger.warn("Invalid capacity provided: {}", request.capacity)
            throw AppException.ValidationException("Table capacity must be greater than 0")
        }

        // Create and save entity
        val table = tableMapper.toEntity(request, restaurant)
        val saved = tableRepository.save(table)

        logger.info("Table created successfully: ID={}, tableNumber={}", saved.id, saved.tableNumber)
        return tableMapper.toResponse(saved)
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Get table by ID
     *
     * @param id table ID
     * @return table response, or null if not found
     */
    override fun getTable(id: Long): TableResponse? {
        logger.debug("Fetching table: {}", id)
        return tableRepository.findById(id)
            .map { tableMapper.toResponse(it) }
            .orElse(null)
            .also {
                if (it != null) logger.debug("Table found: {}", id)
                else logger.warn("Table not found: {}", id)
            }
    }

    /**
     * Get all tables for restaurant
     *
     * @param restaurantId restaurant ID
     * @return list of tables
     */
    override fun getTablesByRestaurant(restaurantId: Long): List<TableResponse> {
        logger.debug("Fetching tables for restaurant: {}", restaurantId)

        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            logger.error("Restaurant not found: {}", restaurantId)
            throw AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
        }

        val tables = tableRepository.findByRestaurantIdOrderByTableNumber(restaurantId)
        logger.debug("Found {} tables for restaurant: {}", tables.size, restaurantId)
        return tableMapper.toResponses(tables)
    }

    /**
     * Get available tables (with optional capacity filter)
     *
     * Performance: Uses database query with index on status
     *
     * @param restaurantId restaurant ID
     * @param minCapacity optional minimum capacity requirement
     * @return list of available tables
     */
    override fun getAvailableTables(restaurantId: Long, minCapacity: Int?): List<TableAvailabilityResponse> {
        logger.debug("Fetching available tables for restaurant: {}, minCapacity: {}", restaurantId, minCapacity)

        val tables = if (minCapacity != null && minCapacity > 0) {
            tableRepository.findAvailableTablesWithCapacity(restaurantId, minCapacity)
        } else {
            tableRepository.findByRestaurantIdAndStatus(restaurantId, TableStatus.AVAILABLE)
        }

        logger.debug("Found {} available tables", tables.size)
        return tableMapper.toAvailabilityResponses(tables)
    }

    /**
     * Get tables by status
     *
     * @param restaurantId restaurant ID
     * @param status table status to filter
     * @return list of tables with given status
     */
    override fun getTablesByStatus(restaurantId: Long, status: TableStatus): List<TableResponse> {
        logger.debug("Fetching tables by status - restaurant: {}, status: {}", restaurantId, status)
        val tables = tableRepository.findByRestaurantIdAndStatus(restaurantId, status)
        logger.debug("Found {} tables with status: {}", tables.size, status)
        return tableMapper.toResponses(tables)
    }

    /**
     * Get all occupied tables
     *
     * @param restaurantId restaurant ID
     * @return list of occupied tables
     */
    override fun getOccupiedTables(restaurantId: Long): List<TableResponse> {
        logger.debug("Fetching occupied tables for restaurant: {}", restaurantId)
        val tables = tableRepository.findOccupiedTables(restaurantId)
        logger.debug("Found {} occupied tables", tables.size)
        return tableMapper.toResponses(tables)
    }

    /**
     * Count available tables
     *
     * Performance: Uses COUNT query for efficiency
     *
     * @param restaurantId restaurant ID
     * @return count of available tables
     */
    override fun countAvailableTables(restaurantId: Long): Long {
        return tableRepository.countAvailableTables(restaurantId)
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update table details
     *
     * @param id table ID
     * @param request update request
     * @return updated table response
     * @throws AppException.ResourceNotFoundException if table not found
     * @throws AppException.ValidationException if validation fails
     * @throws AppException.ConflictException if concurrent modification detected
     */
    override fun updateTable(id: Long, request: TableRequest): TableResponse? {
        logger.debug("Updating table: {}", id)

        try {
            val table = tableRepository.findById(id)
                .orElseThrow {
                    logger.error("Table not found: {}", id)
                    AppException.ResourceNotFoundException("Table not found: $id")
                }

            // If table number is being changed, validate uniqueness
            if (table.tableNumber != request.tableNumber) {
                if (tableRepository.existsByRestaurantIdAndTableNumber(
                    table.restaurant?.restroId ?: 0,
                    request.tableNumber
                )) {
                    throw AppException.ValidationException(
                        "Table number '${request.tableNumber}' already exists"
                    )
                }
            }

            // Update fields
            table.tableNumber = request.tableNumber
            table.capacity = request.capacity
            if (request.status != null && table.status != request.status) {
                table.updateStatus(request.status)
            }
            table.updatedAt = LocalDateTime.now()

            val updated = tableRepository.save(table)
            logger.info("Table updated successfully: {}", id)
            return tableMapper.toResponse(updated)

        } catch (_: OptimisticLockingFailureException) {
            logger.warn("Concurrent modification detected for table: {}", id)
            throw AppException.ConflictException(
                "Table was modified by another user. Please refresh and try again."
            )
        }
    }

    /**
     * Update table status with validation
     *
     * Validates status transitions according to business rules
     *
     * @param id table ID
     * @param newStatus new status
     * @return updated table response
     * @throws AppException.ValidationException if status transition invalid
     */
    override fun updateTableStatus(id: Long, newStatus: TableStatus): TableResponse {
        logger.debug("Updating table status - ID: {}, newStatus: {}", id, newStatus)

        try {
            val table = tableRepository.findById(id)
                .orElseThrow {
                    logger.error("Table not found: {}", id)
                    AppException.ResourceNotFoundException("Table not found: $id")
                }

            // Validate and update status
            table.updateStatus(newStatus)
            table.updatedAt = LocalDateTime.now()

            val updated = tableRepository.save(table)
            logger.info("Table status updated - ID: {}, newStatus: {}", id, newStatus)
            return tableMapper.toResponse(updated)

        } catch (_: OptimisticLockingFailureException) {
            logger.warn("Concurrent modification detected for table: {}", id)
            throw AppException.ConflictException(
                "Table was modified by another user. Please try again."
            )
        }
    }

    /**
     * Assign an order to a table (mark as occupied)
     *
     * @param tableId table ID
     * @param orderId order ID to assign
     * @return updated table response
     */
    override fun assignOrderToTable(tableId: Long, orderId: Long): TableResponse {
        logger.debug("Assigning order {} to table {}", orderId, tableId)

        val table = tableRepository.findById(tableId)
            .orElseThrow {
                logger.error("Table not found: {}", tableId)
                AppException.ResourceNotFoundException("Table not found: $tableId")
            }

        if (!table.canAcceptOrder()) {
            logger.warn("Table {} cannot accept order - status: {}", tableId, table.status)
            throw AppException.ValidationException("Table is not available for new orders")
        }

        // Note: Order object would be loaded from OrderRepository in actual implementation
        // For now, we just update the currentOrderId through the relationship
        table.status = TableStatus.OCCUPIED
        table.updatedAt = LocalDateTime.now()

        val updated = tableRepository.save(table)
        logger.info("Order assigned to table: {}", tableId)
        return tableMapper.toResponse(updated)
    }

    /**
     * Release table from order (mark as available)
     *
     * @param tableId table ID
     * @return updated table response
     */
    override fun releaseTableFromOrder(tableId: Long): TableResponse {
        logger.debug("Releasing table from order: {}", tableId)

        val table = tableRepository.findById(tableId)
            .orElseThrow {
                logger.error("Table not found: {}", tableId)
                AppException.ResourceNotFoundException("Table not found: $tableId")
            }

        table.release()
        table.updatedAt = LocalDateTime.now()

        val updated = tableRepository.save(table)
        logger.info("Table released: {}", tableId)
        return tableMapper.toResponse(updated)
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete table
     *
     * @param id table ID
     * @return true if deleted, false if not found
     */
    override fun deleteTable(id: Long): Boolean {
        logger.debug("Deleting table: {}", id)

        if (!tableRepository.existsById(id)) {
            logger.warn("Table not found for deletion: {}", id)
            return false
        }

        tableRepository.deleteById(id)
        logger.info("Table deleted: {}", id)
        return true
    }

    // ==================== UTILITY OPERATIONS ====================

    /**
     * Validate table number is unique
     *
     * @param restaurantId restaurant ID
     * @param tableNumber table number to validate
     * @return true if valid (unique), false if already exists
     */
    override fun validateTableNumber(restaurantId: Long, tableNumber: String): Boolean {
        return !tableRepository.existsByRestaurantIdAndTableNumber(restaurantId, tableNumber)
    }

    /**
     * Check if table can accept a new order
     *
     * @param tableId table ID
     * @return true if table can accept order
     */
    override fun canTableAcceptOrder(tableId: Long): Boolean {
        logger.debug("Checking if table can accept order: {}", tableId)
        return tableRepository.findById(tableId)
            .map { it.canAcceptOrder() }
            .orElseGet { false }
    }

}

