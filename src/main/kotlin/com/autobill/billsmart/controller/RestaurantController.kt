package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.dto.RestaurantResponse
import com.autobill.billsmart.security.TenantUtils
import com.autobill.billsmart.services.RestaurantService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * RestaurantController
 *
 * GET   /api/v1/restaurants          → list all restaurants (use this to find your restaurant_id)
 * GET   /api/v1/restaurants/{id}     → get one restaurant by ID
 * POST  /api/v1/restaurants          → create a new restaurant
 * PATCH /api/v1/restaurants/{id}     → update restaurant details and settings
 */
@RestController
@RequestMapping("/api/v1/restaurants")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class RestaurantController(
    private val restaurantService: RestaurantService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * GET /api/v1/restaurants
     * Returns all restaurants. Use the `id` from this response as `restaurant_id` in every other API.
     */
    @GetMapping
    fun getAllRestaurants(): ResponseEntity<ApiResponse<List<RestaurantResponse>>> {
        log.info("GET all restaurants")
        val list = restaurantService.getAllRestaurants().map { restaurantService.toResponse(it) }
        return ResponseEntity.ok(ApiResponse.success(list, "Restaurants retrieved successfully"))
    }

    /**
     * GET /api/v1/restaurants/{id}
     * Returns one restaurant by ID.
     */
    @GetMapping("/{id}")
    fun getRestaurant(@PathVariable id: Long): ResponseEntity<ApiResponse<RestaurantResponse>> {
        log.info("GET restaurant id={}", id)
        TenantUtils.assertTenantAccess(id)
        val restaurant = restaurantService.getRestaurant(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", "Restaurant not found with ID: $id"))
        return ResponseEntity.ok(ApiResponse.success(restaurantService.toResponse(restaurant), "Restaurant retrieved successfully"))
    }

    /**
     * POST /api/v1/restaurants
     * Creates a new restaurant. The returned `id` is your restaurant_id for all other APIs.
     */
    @PostMapping
    fun createRestaurant(
        @Valid @RequestBody request: RestaurantRequest
    ): ResponseEntity<ApiResponse<RestaurantResponse>> {
        log.info("POST create restaurant: {}", request.outletName)
        val restaurant = restaurantService.createRestaurant(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(restaurantService.toResponse(restaurant), "Restaurant created successfully"))
    }

    /**
     * PATCH /api/v1/restaurants/{id}
     * Updates restaurant details (name, manager, address).
     * Restricted to ADMIN role (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}")
    fun updateRestaurant(
        @PathVariable id: Long,
        @Valid @RequestBody request: RestaurantRequest
    ): ResponseEntity<ApiResponse<RestaurantResponse>> {
        log.info("PATCH update restaurant id={}", id)
        TenantUtils.assertTenantAccess(id)
        val restaurant = restaurantService.updateRestaurant(id, request)
        return ResponseEntity.ok(ApiResponse.success(restaurantService.toResponse(restaurant), "Restaurant updated successfully"))
    }
}
