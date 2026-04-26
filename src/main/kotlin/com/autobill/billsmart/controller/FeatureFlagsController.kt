package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.FeatureFlagsResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.security.TenantUtils
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * FeatureFlagsController
 *
 * GET /api/v1/restaurants/{restaurantId}/feature-flags
 *
 * Returns per-restaurant feature flags. Mobile reads these after login to drive UI behaviour.
 * Mobile falls back to safe defaults if this endpoint is unavailable or a key is missing:
 *   - is_table_management_enabled = true
 *   - is_pay_before_seat_enabled  = false
 */
@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/feature-flags")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class FeatureFlagsController(
    private val restaurantRepository: RestaurantRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * GET /api/v1/restaurants/{restaurantId}/feature-flags
     *
     * Returns all feature flags for the given restaurant.
     * Flags that are not yet implemented are returned with their safe default value.
     */
    @GetMapping
    fun getFeatureFlags(
        @PathVariable restaurantId: Long
    ): ResponseEntity<ApiResponse<FeatureFlagsResponse>> {
        logger.debug("Fetching feature flags for restaurant: {}", restaurantId)
        TenantUtils.assertTenantAccess(restaurantId)

        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow {
                logger.error("Restaurant not found: {}", restaurantId)
                AppException.ResourceNotFoundException("Restaurant not found: $restaurantId")
            }

        val flags = mapOf(
            // ── Operational flags (stored per-restaurant in DB) ──
            "is_table_management_enabled"  to restaurant.isTableManagementEnabled,
            "is_pay_before_seat_enabled"   to restaurant.isPayBeforeSeatEnabled,

            // ── Feature flags (hardcoded defaults for now — promote to DB columns when needed) ──
            "is_offline_order_sync_enabled" to false,
            "is_realtime_updates_enabled"   to true,
            "is_kitchen_display_enabled"    to true,
            "is_bill_printing_enabled"      to true,
            "is_bill_discount_enabled"      to true,
            "is_split_payment_enabled"      to false,
            "is_sales_reports_enabled"      to true,
            "is_online_order_enabled"       to true
        )

        logger.debug("Feature flags returned for restaurant {}: {}", restaurantId, flags)
        return ResponseEntity.ok(
            ApiResponse.success(
                FeatureFlagsResponse(flags = flags),
                "Feature flags retrieved successfully"
            )
        )
    }
}

