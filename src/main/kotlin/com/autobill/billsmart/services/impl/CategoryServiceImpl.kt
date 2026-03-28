package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.CategoryRequest
import com.autobill.billsmart.dto.CategoryResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.mappers.CategoryMapper
import com.autobill.billsmart.model.Category
import com.autobill.billsmart.repositories.CategoryRepository
import com.autobill.billsmart.repositories.RestaurantRepository
import com.autobill.billsmart.services.CategoryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val restaurantRepository: RestaurantRepository,
    private val categoryMapper: CategoryMapper
) : CategoryService {

    private val log = LoggerFactory.getLogger(CategoryServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun getCategories(restaurantId: Long): List<CategoryResponse> {
        log.debug("Fetching categories for restaurant: {}", restaurantId)

        // Verify restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow { AppException.ResourceNotFoundException("Restaurant not found with ID: $restaurantId") }

        val categories = categoryRepository.findActiveByRestaurantId(restaurantId)
        log.debug("Found {} categories for restaurant: {}", categories.size, restaurantId)

        return categories.map { categoryMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getCategory(id: Long): CategoryResponse? {
        log.debug("Fetching category by ID: {}", id)

        return categoryRepository.findById(id)
            .map { categoryMapper.toResponse(it) }
            .orElse(null)
    }

    @Transactional
    override fun createCategory(restaurantId: Long, request: CategoryRequest): CategoryResponse {
        log.info("Creating category for restaurant: {}", restaurantId)

        // Verify restaurant exists
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { AppException.ResourceNotFoundException("Restaurant not found with ID: $restaurantId") }

        // Check if category name already exists for this restaurant
        if (categoryRepository.findByNameAndRestaurantId(request.name, restaurantId) != null) {
            throw AppException.ConflictException("Category with name '${request.name}' already exists for this restaurant")
        }

        val category = Category().apply {
            this.name = request.name
            this.description = request.description
            this.displayOrder = request.displayOrder
            this.imageUrl = request.imageUrl
            this.isActive = true
            this.restaurant = restaurant
        }

        val saved = categoryRepository.save(category)
        log.info("Category created successfully with ID: {}", saved.id)

        return categoryMapper.toResponse(saved)
    }

    @Transactional
    override fun updateCategory(id: Long, request: CategoryRequest): CategoryResponse? {
        log.info("Updating category with ID: {}", id)

        val category = categoryRepository.findById(id).orElse(null) ?: return null

        category.name = request.name
        category.description = request.description
        category.displayOrder = request.displayOrder
        category.imageUrl = request.imageUrl

        val updated = categoryRepository.save(category)
        log.info("Category updated successfully with ID: {}", updated.id)

        return categoryMapper.toResponse(updated)
    }

    @Transactional
    override fun deleteCategory(id: Long): Boolean {
        log.info("Deleting category with ID: {}", id)

        if (!categoryRepository.existsById(id)) return false

        categoryRepository.deleteById(id)
        log.info("Category deleted successfully with ID: {}", id)

        return true
    }
}
