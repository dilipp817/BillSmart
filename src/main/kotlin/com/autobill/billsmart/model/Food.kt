package com.smart.billsmart.model

import org.springframework.stereotype.Component

@Component
class Food {
    var productId: Int = 0
    var productName: String? = null
    var displayname: String? = null
    var timing: String? = null
}
