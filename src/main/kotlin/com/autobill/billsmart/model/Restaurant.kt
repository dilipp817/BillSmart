package com.autobill.billsmart.model

import com.smart.billsmart.model.Food
import com.smart.billsmart.model.StoreLogo
import jakarta.persistence.*

@Entity
@Table(name = "restaurant")
class Restaurant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var restroId: Long? = null,

    var outletName: String = "",
    var displayname: String = "",
    var outletManager: String = "",

    @Embedded
    var storeAddress: Address = Address(),

    @Transient
    var foods: ArrayList<Food> = arrayListOf(),

    @Transient
    var storeLogo: StoreLogo = StoreLogo()
)
