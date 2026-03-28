package com.autobill.billsmart.dto

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Standard API Response wrapper for all endpoints
 * Provides consistent response format with success status, data, and error details
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetails? = null,
    val message: String? = null
) {
    companion object {
        /**
         * Create a successful response
         */
        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        /**
         * Create an error response
         */
        fun <T> error(
            code: String,
            message: String,
            details: Map<String, Any>? = null
        ): ApiResponse<T> =
            ApiResponse(
                success = false,
                error = ErrorDetails(code = code, message = message, details = details)
            )
    }
}

/**
 * Error details included in error responses
 */
data class ErrorDetails(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

