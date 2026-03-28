package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "restaurant")
class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var restroId: Long? = null

    @field:NotBlank
    @Column(name = "outlet_name", nullable = false)
    var outletName: String = ""

    @field:NotBlank
    @Column(name = "display_name", nullable = false)
    var displayname: String = ""

    @field:NotBlank
    @Column(name = "outlet_manager", nullable = false)
    var outletManager: String = ""

    @Embedded
    var storeAddress: Address = Address()

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var foods: MutableList<Food> = mutableListOf()

    @Embedded
    var storeLogo: StoreLogo = StoreLogo()
}
