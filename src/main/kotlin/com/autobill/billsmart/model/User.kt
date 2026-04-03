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
    var isActive: Boolean = true
)
