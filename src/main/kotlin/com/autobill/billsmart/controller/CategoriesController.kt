package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.CategoryRequest
import com.autobill.billsmart.dto.CategoryResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.services.CategoryService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Categories Controller
 * Handles food categories endpoints
 */
@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class CategoriesController(
    private val categoryService: CategoryService
) {

    private val log = LoggerFactory.getLogger(CategoriesController::class.java)

    /**
     * GET /api/v1/categories
     * Get all categories for a restaurant
     *
     * @param restaurantId Restaurant ID (required)
     */
    @GetMapping
    fun getCategories(
        @RequestParam restaurantId: Long
    ): ResponseEntity<ApiResponse<List<CategoryResponse>>> {
        log.info("Getting categories for restaurant: {}", restaurantId)

        return try {
            val categories = categoryService.getCategories(restaurantId)
            ResponseEntity.ok(
                ApiResponse.success(categories, "Categories retrieved successfully")
            )
        } catch (e: AppException) {
            log.error("Error retrieving categories", e)
            throw e
        }
    }

    /**
     * GET /api/v1/categories/:id
     * Get category by ID
     *
     * @param id Category ID
     */
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<ApiResponse<CategoryResponse>> {
        log.info("Getting category by ID: {}", id)

        val category = categoryService.getCategory(id)
            ?: throw AppException.ResourceNotFoundException("Category not found with ID: $id")

        return ResponseEntity.ok(
            ApiResponse.success(category, "Category retrieved successfully")
        )
    }

    /**
     * POST /api/v1/categories
     * Create new category for a restaurant
     *
     * @param restaurantId Restaurant ID
     * @param request CategoryRequest with category details
     */
    @PostMapping
    fun createCategory(
        @RequestParam restaurantId: Long,
        @Valid @RequestBody request: CategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        log.info("Creating category for restaurant: {}", restaurantId)

        if (restaurantId <= 0) {
            throw AppException.ValidationException("Invalid restaurant ID")
        }

        return try {
            val response = categoryService.createCategory(restaurantId, request)
            ResponseEntity(
                ApiResponse.success(response, "Category created successfully"),
                HttpStatus.CREATED
            )
        } catch (e: AppException) {
            log.error("Error creating category", e)
            throw e
        }
    }

    /**
     * PUT /api/v1/categories/{id}
     * Update category
     */
    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: CategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        log.info("Updating category with ID: {}", id)

        val updated = categoryService.updateCategory(id, request)
            ?: throw AppException.ResourceNotFoundException("Category not found with ID: $id")

        return ResponseEntity.ok(
            ApiResponse.success(updated, "Category updated successfully")
        )
    }

    /**
     * DELETE /api/v1/categories/{id}
     * Delete category
     */
    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        log.info("Deleting category with ID: {}", id)

        val deleted = categoryService.deleteCategory(id)
        if (!deleted) {
            throw AppException.ResourceNotFoundException("Category not found with ID: $id")
        }

        return ResponseEntity.ok(
            ApiResponse.success("", "Category deleted successfully")
        )
    }
}

