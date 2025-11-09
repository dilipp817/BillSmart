package com.autobill.billsmart.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class StaffRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val gender: String,

    @field:NotBlank
    val age: String,

    @field:NotBlank
    val salary: String,

    @field:Valid
    val address: AddressRequest
)
