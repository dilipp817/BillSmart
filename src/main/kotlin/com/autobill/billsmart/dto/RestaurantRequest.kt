package com.autobill.billsmart.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RestaurantRequest(
    @field:NotBlank(message = "Outlet name is required")
    @field:Size(min = 2, max = 255, message = "Outlet name must be between 2 and 255 characters")
    val outletName: String,

    @field:NotBlank(message = "Display name is required")
    @field:Size(min = 2, max = 255, message = "Display name must be between 2 and 255 characters")
    val displayname: String,

    @field:NotBlank(message = "Outlet manager name is required")
    @field:Size(min = 2, max = 255, message = "Manager name must be between 2 and 255 characters")
    val outletManager: String,

    @field:Valid(message = "Invalid store address")
    val storeAddress: AddressRequest
)

