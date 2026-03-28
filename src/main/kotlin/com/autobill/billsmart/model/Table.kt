package com.autobill.billsmart.model

import com.autobill.billsmart.model.enums.TableStatus
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Table Entity - Represents physical restaurant tables
 *
 * Business Rules:
 * - Each table belongs to exactly one restaurant
 * - A table can have at most one active order at a time
 * - Table status determines availability
 *
 * Thread Safety: Handled via @Version optimistic locking
 * Performance: Indexes on restaurant_id and status for fast queries
 */
@Entity
@jakarta.persistence.Table(
    name = "tables",
    indexes = [
        Index(name = "idx_tables_restaurant", columnList = "restaurant_id"),
        Index(name = "idx_tables_status", columnList = "status"),
        Index(name = "idx_tables_restaurant_number", columnList = "restaurant_id,table_number", unique = false)
    ]
)
class Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: Restaurant? = null

    @Column(nullable = false, length = 50)
    var tableNumber: String = ""

    @Column(nullable = false)
    var capacity: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TableStatus = TableStatus.AVAILABLE

    /**
     * Current order ID (if table is occupied)
     * Relationship with Order table via OneToOne
     */
    @OneToOne(cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    @JoinColumn(name = "current_order_id")
    var currentOrder: Order? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    /**
     * Version field for optimistic locking
     * Prevents concurrent modification issues
     */
    @Version
    @Column(name = "version")
    var version: Long = 0

    // Business Logic Methods

    /**
     * Check if table can accept a new order
     *
     * @return true if table is available
     */
    fun canAcceptOrder(): Boolean {
        return status == TableStatus.AVAILABLE && currentOrder == null
    }

    /**
     * Mark table as occupied with new order
     *
     * @param order The order occupying this table
     * @throws IllegalStateException if table is not available
     */
    fun occupyWithOrder(order: Order) {
        if (!canAcceptOrder()) {
            throw IllegalStateException("Table is not available for new orders")
        }
        this.currentOrder = order
        this.status = TableStatus.OCCUPIED
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Mark table as available after order completion
     */
    fun release() {
        this.currentOrder = null
        this.status = TableStatus.AVAILABLE
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Update table status
     * Validates status transitions
     *
     * @param newStatus the new status
     * @throws IllegalArgumentException if status transition is invalid
     */
    fun updateStatus(newStatus: TableStatus) {
        // Validate status transitions
        val validTransitions: List<TableStatus> = when (status) {
            TableStatus.AVAILABLE -> listOf(TableStatus.OCCUPIED, TableStatus.RESERVED, TableStatus.CLEANING)
            TableStatus.OCCUPIED -> listOf(TableStatus.AVAILABLE, TableStatus.CLEANING)
            TableStatus.RESERVED -> listOf(TableStatus.OCCUPIED, TableStatus.AVAILABLE)
            TableStatus.CLEANING -> listOf(TableStatus.AVAILABLE)
            TableStatus.MAINTENANCE -> listOf(TableStatus.AVAILABLE)
        }

        if (!validTransitions.contains(newStatus)) {
            throw IllegalArgumentException(
                "Cannot transition from $status to $newStatus"
            )
        }

        this.status = newStatus
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Check if table matches search criteria
     *
     * @param minCapacity minimum seats required
     * @return true if table meets capacity requirement
     */
    fun meetsCapacity(minCapacity: Int): Boolean {
        return capacity >= minCapacity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Table) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Table(id=$id, tableNumber='$tableNumber', capacity=$capacity, status=$status)"
    }
}

