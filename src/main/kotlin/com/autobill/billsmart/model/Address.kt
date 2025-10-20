package com.smart.billsmart.model

import org.springframework.stereotype.Component

@Component
class Address {
    var building: String? = null
    var street: String? = null
    var storelocation: String? = null
    var zipCode: Int = 0

    fun show() {
        println("My address is building $building street $street")
    }
}
