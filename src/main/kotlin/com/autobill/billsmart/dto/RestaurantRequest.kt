package com.autobill.billsmart.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class RestaurantRequest(
    @field:NotBlank
    val outletName: String,

    @field:NotBlank
    val displayname: String,

    @field:NotBlank
    val outletManager: String,

    @field:Valid
    val storeAddress: AddressRequest
)

