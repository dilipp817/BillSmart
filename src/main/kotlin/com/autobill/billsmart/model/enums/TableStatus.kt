package com.autobill.billsmart.model.enums

/**
 * TableStatus Enum - Represents all possible states of a restaurant table
 *
 * States:
 * - AVAILABLE: Table is ready for new customers
 * - OCCUPIED: Table has active customers and order
 * - RESERVED: Table is reserved for future booking
 * - CLEANING: Table is being cleaned between customers
 * - MAINTENANCE: Table is under maintenance and not usable
 */
enum class TableStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    CLEANING,
    MAINTENANCE;

    /**
     * Check if table is available for new orders
     */
    fun isAvailableForOrders(): Boolean = this == AVAILABLE

    /**
     * Check if table is actively in use
     */
    fun isInUse(): Boolean = this == OCCUPIED

    /**
     * Check if table is unusable (maintenance/cleaning)
     */
    fun isUnavailable(): Boolean = this in listOf(CLEANING, MAINTENANCE)
}

