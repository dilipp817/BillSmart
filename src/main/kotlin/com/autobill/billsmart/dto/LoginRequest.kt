package com.autobill.billsmart.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Login Request DTO
 * Used for user authentication
 */
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String
)

/**
 * Login Response DTO
 * Returns user info and JWT token after successful authentication
 */
data class LoginResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val token: String,
    val expiresIn: Long = 86400 // 24 hours in seconds
)

/**
 * JWT Token Response DTO
 * Used when token is refreshed or validated
 */
data class TokenResponse(
    val token: String,
    val expiresIn: Long = 86400
)

/**
 * User Info DTO
 * Returns basic user information
 */
data class UserInfoResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)

