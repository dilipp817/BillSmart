package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "food")
class Food(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotBlank
    @Column(nullable = false)
    var name: String = "",

    @Column
    var price: Double? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restro_id")
    var restaurant: Restaurant? = null
)
