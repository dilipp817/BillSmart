package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

@Entity
@jakarta.persistence.Table(name = "categories", indexes = [
    Index(name = "idx_categories_restaurant_id", columnList = "restaurant_id"),
    Index(name = "idx_categories_active", columnList = "is_active")
])
class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @field:NotBlank(message = "Category name is required")
    @Column(nullable = false, length = 100)
    var name: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @field:Positive
    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: Restaurant? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PostPersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

