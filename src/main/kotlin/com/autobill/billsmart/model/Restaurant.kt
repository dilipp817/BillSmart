package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

/**
 * Restaurant Entity - Represents a restaurant/outlet
 *
 * Primary key is `restroId` (Long) — all other entities reference restaurant via this field.
 * This naming is intentional and used throughout mappers, repositories and DTOs.
 */
@Entity
@jakarta.persistence.Table(
    name = "restaurant",
    indexes = [
        Index(name = "idx_restaurant_outlet_name", columnList = "outlet_name")
    ]
)
class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restro_id")
    var restroId: Long? = null

    @field:NotBlank(message = "Outlet name is required")
    @Column(name = "outlet_name", nullable = false, length = 255)
    var outletName: String = ""

    @field:NotBlank(message = "Display name is required")
    @Column(name = "displayname", nullable = false, length = 255)
    var displayname: String = ""

    @field:NotBlank(message = "Outlet manager name is required")
    @Column(name = "outlet_manager", nullable = false, length = 255)
    var outletManager: String = ""

    @Embedded
    @field:Valid
    var storeAddress: Address? = null

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var foods: MutableList<Food> = mutableListOf()

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var categories: MutableList<Category> = mutableListOf()

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var tables: MutableList<Table> = mutableListOf()

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    /**
     * When false: counter-service / fast-food mode — no table selection, all orders go straight to Place Order.
     * When true (default): staff selects a table for dine-in orders.
     */
    @Column(name = "is_table_management_enabled", nullable = false)
    var isTableManagementEnabled: Boolean = true

    /**
     * Only relevant when isTableManagementEnabled=true AND order_type=DINE_IN.
     * When true: payment collected upfront at counter before seating.
     * When false (default): payment collected post-meal via Billing screen.
     */
    @Column(name = "is_pay_before_seat_enabled", nullable = false)
    var isPayBeforeSeatEnabled: Boolean = false

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

