package com.autobill.billsmart.dto

/**
 * FeatureFlagsResponse - Response for GET /api/v1/restaurants/{restaurantId}/feature-flags
 *
 * All flags default to the safe/conservative value so mobile gracefully degrades
 * when the endpoint is unreachable or a key is absent.
 */
data class FeatureFlagsResponse(
    val flags: Map<String, Boolean>
)

