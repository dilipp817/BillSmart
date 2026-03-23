package com.autobill.billsmart.exception

/**
 * Base exception for all domain-level exceptions
 */
sealed class AppException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {

    /**
     * Thrown when input validation fails
     */
    class ValidationException(message: String) : AppException(message)

    /**
     * Thrown when a requested resource is not found
     */
    class ResourceNotFoundException(message: String) : AppException(message)

    /**
     * Thrown when user is not authenticated
     */
    class UnauthorizedException(message: String) : AppException(message)

    /**
     * Thrown when user doesn't have permission to perform action
     */
    class ForbiddenException(message: String) : AppException(message)

    /**
     * Thrown when there's a conflict (e.g., duplicate entry)
     */
    class ConflictException(message: String) : AppException(message)

    /**
     * Thrown when database operation fails
     */
    class PersistenceException(
        message: String,
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Thrown for unexpected server errors
     */
    class InternalServerException(
        message: String,
        cause: Throwable? = null
    ) : AppException(message, cause)
}

