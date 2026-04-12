package com.autobill.billsmart.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Login Request DTO
 * Used for user authentication.
 * Optionally accepts device tracking fields (v2 feature).
 */
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String,

    /** Physical device identifier — tablet-counter-1, etc. (stored for multi-device tracking v2) */
    val deviceId: String? = null,

    /** Device type: tablet | mobile | desktop (v2) */
    val deviceType: String? = null
)

/**
 * Login Response DTO
 * Returns user info and JWT token after successful authentication.
 *
 * IMPORTANT for mobile team:
 *   - Save [restaurantId] immediately after login.
 *   - Use it for every subsequent API call: /api/v1/restaurants/{restaurantId}/...
 *   - Never hardcode restaurantId = 1.
 *   - If restaurantId is null the account is a super_admin (show admin dashboard).
 */
data class LoginResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    /** Which outlet this user belongs to. null only for super_admin. */
    val restaurantId: Long?,
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
 * Returns basic user information (used by GET /api/v1/auth/me)
 */
data class UserInfoResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    /** Which outlet this user belongs to. null only for super_admin. */
    val restaurantId: Long?
)

