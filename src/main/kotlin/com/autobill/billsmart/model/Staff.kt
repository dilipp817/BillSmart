package com.smart.billsmart.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Staff {
    var id: String? = null
    var name: String? = null
    var gender: String? = null
    var age: String? = null
    var salary: String? = null

    @Autowired
    lateinit var address: Address
}
