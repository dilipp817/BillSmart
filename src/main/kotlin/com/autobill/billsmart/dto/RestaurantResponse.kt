package com.autobill.billsmart.dto

import java.time.LocalDateTime

/**
 * AddressResponse DTO
 * Nested address object — consistent with AddressRequest shape used in POST/PATCH.
 */
data class AddressResponse(
    val building: String?,
    val street: String?,
    val location: String?,
    val zipCode: String?
)

/**
 * RestaurantResponse DTO
 * Returned by GET /api/v1/restaurants and GET /api/v1/restaurants/{id}
 *
 * Wire format (snake_case via global Jackson config):
 *   outlet_name, displayname, outlet_manager, address.{ building, street, location, zip_code }
 */
data class RestaurantResponse(
    val id: Long,
    val outletName: String,
    val displayname: String,
    val outletManager: String,
    /** Nested address — same shape as the address object in the create/update request. */
    val address: AddressResponse?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
