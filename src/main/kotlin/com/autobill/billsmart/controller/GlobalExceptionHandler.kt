package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.exception.AppException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * Global exception handler for all REST endpoints
 * Catches exceptions and returns standardized error responses
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(AppException.ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: AppException.ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Resource not found: {}", ex.message)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "RESOURCE_NOT_FOUND",
                message = ex.message ?: "Resource not found"
            ),
            HttpStatus.NOT_FOUND
        )
    }

    /**
     * Handle ValidationException
     */
    @ExceptionHandler(AppException.ValidationException::class)
    fun handleValidation(
        ex: AppException.ValidationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Validation error: {}", ex.message)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "VALIDATION_ERROR",
                message = ex.message ?: "Validation failed"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(AppException.UnauthorizedException::class)
    fun handleUnauthorized(
        ex: AppException.UnauthorizedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Unauthorized access: {}", ex.message)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "UNAUTHORIZED",
                message = ex.message ?: "Unauthorized"
            ),
            HttpStatus.UNAUTHORIZED
        )
    }

    /**
     * Handle ForbiddenException
     */
    @ExceptionHandler(AppException.ForbiddenException::class)
    fun handleForbidden(
        ex: AppException.ForbiddenException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Forbidden access: {}", ex.message)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "FORBIDDEN",
                message = ex.message ?: "Forbidden"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    /**
     * Handle ConflictException
     */
    @ExceptionHandler(AppException.ConflictException::class)
    fun handleConflict(
        ex: AppException.ConflictException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Conflict: {}", ex.message)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "CONFLICT",
                message = ex.message ?: "Conflict occurred"
            ),
            HttpStatus.CONFLICT
        )
    }

    /**
     * Handle PersistenceException
     */
    @ExceptionHandler(AppException.PersistenceException::class)
    fun handlePersistence(
        ex: AppException.PersistenceException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Database error: {}", ex.message, ex)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "DATABASE_ERROR",
                message = "Database operation failed"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    /**
     * Handle InternalServerException
     */
    @ExceptionHandler(AppException.InternalServerException::class)
    fun handleInternalServer(
        ex: AppException.InternalServerException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Internal server error: {}", ex.message, ex)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    /**
     * Handle validation errors from @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Validation error in request body")
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.fieldErrors.forEach { error ->
            errors[error.field] = error.defaultMessage ?: "Invalid value"
        }
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "VALIDATION_ERROR",
                message = "Request validation failed",
                details = errors as Map<String, Any>
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected error: {}", ex.message, ex)
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}

