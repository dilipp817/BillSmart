package com.autobill.billsmart.dto

import jakarta.validation.constraints.NotBlank

data class AddressRequest(
    @field:NotBlank
    val building: String,

    @field:NotBlank
    val street: String,

    @field:NotBlank
    val storelocation: String,

    @field:NotBlank
    val zipCode: String
)

