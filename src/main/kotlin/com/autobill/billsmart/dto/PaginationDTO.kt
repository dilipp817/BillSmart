package com.autobill.billsmart.dto

/**
 * Pagination request parameters
 */
data class PaginationParams(
    val page: Int = 0,
    val limit: Int = 20,
    val sortBy: String = "id",
    val sortOrder: String = "ASC"
) {
    init {
        require(page >= 0) { "Page must be >= 0" }
        require(limit in 1..100) { "Limit must be between 1 and 100" }
    }
}

/**
 * Generic paginated response wrapper
 */
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationMeta
)

/**
 * Pagination metadata
 */
data class PaginationMeta(
    val currentPage: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

