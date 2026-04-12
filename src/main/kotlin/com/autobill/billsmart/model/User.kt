package com.autobill.billsmart.model

import jakarta.persistence.*

@Entity
@jakarta.persistence.Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(nullable = false, length = 50)
    var role: String = "staff",

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    /**
     * The restaurant (outlet) this user belongs to.
     * null only for super_admin users who span all restaurants.
     * Set at account creation and embedded in the JWT on every login.
     */
    @Column(name = "restaurant_id")
    var restaurantId: Long? = null,

    /** Android/tablet device identifier — for multi-device tracking (v2). */
    @Column(name = "device_id", length = 255)
    var deviceId: String? = null,

    /** Device type: tablet | mobile | desktop (v2). */
    @Column(name = "device_type", length = 50)
    var deviceType: String? = null
)


