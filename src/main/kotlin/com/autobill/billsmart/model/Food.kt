package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@jakarta.persistence.Table(name = "food")
class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @field:NotBlank
    @Column(nullable = false)
    var name: String = ""

    @Column
    var price: Double? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restro_id")
    var restaurant: Restaurant? = null

    @Column(name = "category_id")
    var categoryId: Long? = null

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null

    @Column(name = "is_available")
    var isAvailable: Boolean = true

    @Column(name = "preparation_time")
    var preparationTime: Int? = null

    @Column(length = 500)
    var allergens: String? = null

    @Column
    var calories: Int? = null

    @Column(name = "is_vegetarian")
    var isVegetarian: Boolean = false

    @Column(name = "is_spicy")
    var isSpicy: Boolean = false

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
}
