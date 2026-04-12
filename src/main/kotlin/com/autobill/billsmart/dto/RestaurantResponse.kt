package com.autobill.billsmart.dto

import java.time.LocalDateTime

/**
 * RestaurantResponse DTO
 * Returned by GET /api/v1/restaurants and GET /api/v1/restaurants/{id}
 */
data class RestaurantResponse(
    val id: Long,
    val outletName: String,
    val displayname: String,
    val outletManager: String,
    val building: String?,
    val street: String?,
    val storeLocation: String?,
    val zipCode: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

