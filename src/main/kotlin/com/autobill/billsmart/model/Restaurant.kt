package com.smart.billsmart.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Restaurant {
    var restroId: Int = 0
    var outletName: String? = null
    var displayname: String? = null
    var outletManager: String? = null

    @Autowired
    lateinit var storeAddress: Address

    var foods: ArrayList<Food>? = null

    @Autowired
    lateinit var storeLogo: StoreLogo
}
