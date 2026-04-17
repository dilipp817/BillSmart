package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.services.FoodService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Foods Controller
 * Handles food item endpoints
 */
@RestController
@RequestMapping("/api/v1/foods")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class FoodsController(
    private val foodService: FoodService
) {

    private val log = LoggerFactory.getLogger(FoodsController::class.java)

    /**
     * GET /api/v1/foods
     * Get all foods with pagination, filtering, and sorting
     *
     * @param offset Starting position (default: 0)
     * @param limit Items per page (default: 20, max: 100)
     * @param restaurantId Filter by restaurant ID (optional)
     * @param categoryId Filter by category ID (optional)
     * @param search Search in food name (optional)
     * @param isVegetarian Filter by vegetarian (optional)
     * @param isSpicy Filter by spicy (optional)
     * @param sort Sort order - format: "field:direction" e.g., "price:asc" (optional)
     */
    @GetMapping
    fun getAllFoods(
        @RequestParam(defaultValue = "0") @Min(0) offset: Int,
        @RequestParam(defaultValue = "20") @Positive @Max(100) limit: Int,
        @RequestParam(name = "restaurant_id", required = false) restaurantId: Long?,
        @RequestParam(name = "category_id", required = false) categoryId: Long?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "is_vegetarian", required = false) isVegetarian: Boolean?,
        @RequestParam(name = "is_spicy", required = false) isSpicy: Boolean?,
        @RequestParam(required = false) sort: String?
    ): ResponseEntity<ApiResponse<PaginatedResponse<FoodListItem>>> {
        log.info("Getting foods - offset: {}, limit: {}, restaurantId: {}, search: {}", offset, limit, restaurantId, search)

        return try {
            // Validate and normalize limit
            val normalizedLimit = if (limit > 100) 100 else limit
            val page = offset / normalizedLimit

            // Get all foods and apply filters in memory
            val allFoods = if (restaurantId != null && restaurantId > 0) {
                foodService.getAllFoods(restaurantId)
            } else {
                emptyList()
            }

            // Apply filters
            var filteredFoods = allFoods

            // Filter by search term
            if (!search.isNullOrBlank()) {
                filteredFoods = filteredFoods.filter {
                    it.name.contains(search, ignoreCase = true)
                }
            }

            // Filter by category
            if (categoryId != null && categoryId > 0) {
                filteredFoods = filteredFoods.filter {
                    it.categoryId == categoryId
                }
            }

            // Filter by vegetarian
            if (isVegetarian != null) {
                filteredFoods = filteredFoods.filter {
                    it.isVegetarian == isVegetarian
                }
            }

            // Filter by spicy
            if (isSpicy != null) {
                filteredFoods = filteredFoods.filter {
                    it.isSpicy == isSpicy
                }
            }

            // Apply sorting
            val sortedFoods = when {
                sort?.lowercase() == "price:asc" -> filteredFoods.sortedBy { it.price }
                sort?.lowercase() == "price:desc" -> filteredFoods.sortedByDescending { it.price }
                sort?.lowercase() == "name:asc" -> filteredFoods.sortedBy { it.name }
                sort?.lowercase() == "name:desc" -> filteredFoods.sortedByDescending { it.name }
                else -> filteredFoods.sortedBy { it.id }
            }

            // Apply pagination
            val total = sortedFoods.size
            val paginatedData = sortedFoods
                .drop(offset)
                .take(normalizedLimit)
                .map { food ->
                    FoodListItem(
                        id = food.id,
                        name = food.name,
                        price = food.price,
                        imageUrl = food.imageUrl,
                        categoryName = food.categoryName,
                        isAvailable = food.isAvailable,
                        isVegetarian = food.isVegetarian,
                        isSpicy = food.isSpicy
                    )
                }

            // Calculate pagination metadata
            val hasMore = (offset + paginatedData.size) < total
            val paginationMeta = PaginationMeta(
                currentPage = page,
                limit = normalizedLimit,
                total = total,
                totalPages = (total + normalizedLimit - 1) / normalizedLimit,
                hasNext = hasMore,
                hasPrevious = offset > 0
            )

            log.debug("Found {} foods, returning {} items", total, paginatedData.size)

            ResponseEntity.ok(
                ApiResponse.success(
                    PaginatedResponse(paginatedData, paginationMeta),
                    "Foods retrieved successfully"
                )
            )
        } catch (e: AppException) {
            log.error("Application error retrieving foods", e)
            throw e
        } catch (e: Exception) {
            log.error("Error retrieving foods", e)
            throw e
        }
    }

    /**
     * GET /api/v1/foods/search
     * Search foods by name with optional filters
     *
     * @param q Search keyword (matches food name, case-insensitive)
     * @param restaurantId Filter by restaurant (optional)
     * @param categoryId Filter by category (optional)
     * @param isVegetarian Filter by vegetarian (optional)
     * @param isSpicy Filter by spicy (optional)
     * @param isAvailable Filter by availability (optional, default true)
     * @param offset Pagination offset (default 0)
     * @param limit Page size (default 20, max 100)
     */
    @GetMapping("/search")
    fun searchFoods(
        @RequestParam(required = false) q: String?,
        @RequestParam(name = "restaurant_id", required = false) restaurantId: Long?,
        @RequestParam(name = "category_id", required = false) categoryId: Long?,
        @RequestParam(name = "is_vegetarian", required = false) isVegetarian: Boolean?,
        @RequestParam(name = "is_spicy", required = false) isSpicy: Boolean?,
        @RequestParam(name = "is_available", required = false) isAvailable: Boolean?,
        @RequestParam(defaultValue = "0") @Min(0) offset: Int,
        @RequestParam(defaultValue = "20") @Positive @Max(100) limit: Int
    ): ResponseEntity<ApiResponse<PaginatedResponse<FoodListItem>>> {
        log.info("Searching foods - q: {}, restaurantId: {}, offset: {}, limit: {}", q, restaurantId, offset, limit)

        return try {
            // B-NEW-2: If restaurant_id not passed, fall back to the restaurantId stored in
            // the JWT authentication details by JwtAuthenticationFilter.
            // Super-admin tokens have null restaurantId — they must pass restaurant_id explicitly.
            val effectiveRestaurantId: Long? = restaurantId
                ?: (SecurityContextHolder.getContext().authentication?.details as? Long)

            if (effectiveRestaurantId == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                        code = "VALIDATION_ERROR",
                        message = "restaurant_id is required"
                    )
                )
            }

            val results = foodService.searchFoods(
                query = q,
                restaurantId = effectiveRestaurantId,
                categoryId = categoryId,
                isVegetarian = isVegetarian,
                isSpicy = isSpicy,
                isAvailable = isAvailable
            )

            val total = results.size
            val paginatedData = results
                .drop(offset)
                .take(limit)
                .map { food ->
                    FoodListItem(
                        id = food.id,
                        name = food.name,
                        price = food.price,
                        imageUrl = food.imageUrl,
                        categoryName = food.categoryName,
                        isAvailable = food.isAvailable,
                        isVegetarian = food.isVegetarian,
                        isSpicy = food.isSpicy
                    )
                }

            val totalPages = if (limit > 0) (total + limit - 1) / limit else 0
            val paginationMeta = PaginationMeta(
                currentPage = if (limit > 0) offset / limit else 0,
                limit = limit,
                total = total,
                totalPages = totalPages,
                hasNext = (offset + paginatedData.size) < total,
                hasPrevious = offset > 0
            )

            ResponseEntity.ok(
                ApiResponse.success(
                    PaginatedResponse(paginatedData, paginationMeta),
                    "Search completed successfully"
                )
            )
        } catch (e: AppException) {
            log.error("Error searching foods", e)
            throw e
        } catch (e: Exception) {
            log.error("Unexpected error searching foods", e)
            throw e
        }
    }

    /**
     * GET /api/v1/foods/:id
     * Get food item by ID
     *
     * @param id Food ID
     */
    @GetMapping("/{id}")
    fun getFoodById(@PathVariable id: Long): ResponseEntity<ApiResponse<FoodResponse>> {
        log.info("Getting food by ID: {}", id)

        val food = foodService.getFood(id)
            ?: throw AppException.ResourceNotFoundException("Food not found with ID: $id")

        // FoodResponse already contains restaurantId mapped from food.restaurant.restroId by FoodMapper
        return ResponseEntity.ok(ApiResponse.success(food, "Food retrieved successfully"))
    }

    /**
     * GET /api/v1/foods/restaurant/:restaurantId
     * Get all foods for a specific restaurant
     *
     * @param restaurantId Restaurant ID
     * @param page Page number
     * @param limit Items per page
     */
    @GetMapping("/restaurant/{restaurantId}")
    fun getFoodsByRestaurant(
        @PathVariable restaurantId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<PaginatedResponse<FoodListItem>>> {
        log.info("Getting foods for restaurant: {}", restaurantId)

        return try {
            val foods = foodService.getAllFoods(restaurantId)

            val paginatedData = foods
                .drop(page * limit)
                .take(limit)
                .map { food ->
                    FoodListItem(
                        id = food.id,
                        name = food.name,
                        price = food.price,
                        imageUrl = food.imageUrl,
                        categoryName = food.categoryName,
                        isAvailable = food.isAvailable,
                        isVegetarian = food.isVegetarian,
                        isSpicy = food.isSpicy
                    )
                }

            val totalPages = (foods.size + limit - 1) / limit
            val paginationMeta = PaginationMeta(
                currentPage = page,
                limit = limit,
                total = foods.size,
                totalPages = totalPages,
                hasNext = (page + 1) < totalPages,
                hasPrevious = page > 0
            )

            ResponseEntity.ok(
                ApiResponse.success(
                    PaginatedResponse(paginatedData, paginationMeta),
                    "Foods retrieved successfully"
                )
            )
        } catch (e: Exception) {
            log.error("Error retrieving foods for restaurant: {}", restaurantId, e)
            throw e
        }
    }

    /**
     * POST /api/v1/foods/restaurant/:restaurantId
     * Create new food item for a restaurant
     *
     * @param restaurantId Restaurant ID
     * @param request FoodRequest with food details
     */
    @PostMapping("/restaurant/{restaurantId}")
    fun createFood(
        @PathVariable restaurantId: Long,
        @Valid @RequestBody request: FoodRequest
    ): ResponseEntity<ApiResponse<FoodResponse>> {
        log.info("Creating food for restaurant: {}", restaurantId)

        return try {
            val response = foodService.createFood(restaurantId, request)
            ResponseEntity(
                ApiResponse.success(response, "Food created successfully"),
                HttpStatus.CREATED
            )
        } catch (e: Exception) {
            log.error("Error creating food", e)
            throw e
        }
    }
}
