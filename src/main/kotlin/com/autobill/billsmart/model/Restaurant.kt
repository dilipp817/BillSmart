package com.autobill.billsmart.model

import com.smart.billsmart.model.Food
import com.smart.billsmart.model.StoreLogo
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "restaurant")
class Restaurant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var restroId: Long? = null,

    @field:NotBlank
    @Column(name = "outlet_name", nullable = false)
    var outletName: String = "",

    @field:NotBlank
    @Column(name = "display_name", nullable = false)
    var displayname: String = "",

    @field:NotBlank
    @Column(name = "outlet_manager", nullable = false)
    var outletManager: String = "",

    @Embedded
    var storeAddress: Address = Address(),

    @Transient
    var foods: ArrayList<Food> = arrayListOf(),

    @Transient
    var storeLogo: StoreLogo = StoreLogo()
)
