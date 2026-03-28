package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.CategoryResponse
import com.autobill.billsmart.model.Category
import org.springframework.stereotype.Component

@Component
class CategoryMapper {

    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id ?: 0L,
            name = category.name,
            description = category.description,
            imageUrl = category.imageUrl,
            displayOrder = category.displayOrder,
            isActive = category.isActive,
            foodCount = 0 // Will be calculated from Food table if needed
        )
    }
}

