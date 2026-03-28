package com.autobill.billsmart.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AddressRequest(
    @field:NotBlank(message = "Building is required")
    @field:Size(min = 1, max = 255, message = "Building must be between 1 and 255 characters")
    val building: String,

    @field:NotBlank(message = "Street is required")
    @field:Size(min = 1, max = 255, message = "Street must be between 1 and 255 characters")
    val street: String,

    @field:NotBlank(message = "Store location is required")
    @field:Size(min = 1, max = 255, message = "Store location must be between 1 and 255 characters")
    val storelocation: String,

    @field:NotBlank(message = "ZIP code is required")
    @field:Size(min = 4, max = 10, message = "ZIP code must be between 4 and 10 characters")
    val zipCode: String
)

