package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.LoginRequest
import com.autobill.billsmart.dto.LoginResponse
import com.autobill.billsmart.dto.UserInfoResponse
import com.autobill.billsmart.security.JwtTokenProvider
import com.autobill.billsmart.services.AuthService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Authentication Controller
 * Handles login and user authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class AuthController(
    private val authService: AuthService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * POST /api/v1/auth/login
     * User login endpoint
     *
     * @param request LoginRequest with username and password
     * @return LoginResponse with user info and JWT token
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        log.info("Login attempt for user: {}", request.username)

        return try {
            val response = authService.login(request)
            log.info("Login successful for user: {}", request.username)
            ResponseEntity.ok(ApiResponse.success(response, "Login successful"))
        } catch (e: Exception) {
            log.error("Login failed for user: {}", request.username, e)
            throw e
        }
    }

    /**
     * GET /api/v1/auth/me
     * Get current user information
     * Requires JWT token in Authorization header
     *
     * @param authHeader Authorization header containing Bearer JWT token
     * @return UserInfoResponse with user details
     */
    @GetMapping("/me")
    fun getCurrentUser(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<ApiResponse<UserInfoResponse>> {
        log.debug("Fetching current user info")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("UNAUTHORIZED", "Authorization header missing or invalid. Expected: Bearer <token>")
            )
        }

        val token = authHeader.substring(7)
        val username = jwtTokenProvider.getUsernameFromToken(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("UNAUTHORIZED", "Token is invalid or expired")
            )

        val userInfo = authService.getCurrentUser(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("RESOURCE_NOT_FOUND", "User not found")
            )

        return ResponseEntity.ok(ApiResponse.success(userInfo, "User info retrieved"))
    }

    /**
     * POST /api/v1/auth/validate
     * Validate JWT token
     *
     * Reads the token from the Authorization Bearer header — the same reliable
     * mechanism used by every other authenticated endpoint. Sending the JWT as
     * a URL query parameter is intentionally avoided: long tokens can be
     * mangled by proxies/load-balancers, and URLs are recorded in server logs,
     * which would expose the token.
     *
     * @param authHeader Authorization header containing Bearer JWT token
     * @return Token claims (valid, username, userId, role) if valid
     */
    @PostMapping("/validate")
    fun validateToken(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.debug("Validating token")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("UNAUTHORIZED", "Authorization header missing or invalid. Expected: Bearer <token>")
            )
        }

        val token = authHeader.substring(7).trim()
        val username     = jwtTokenProvider.getUsernameFromToken(token)
        val userId       = jwtTokenProvider.getUserIdFromToken(token)
        val role         = jwtTokenProvider.getRoleFromToken(token)
        val restaurantId = jwtTokenProvider.getRestaurantIdFromToken(token)

        return if (username != null && userId != null) {
            log.debug("Token validated successfully for user: {}", username)
            val claims = mutableMapOf<String, Any>(
                "valid"       to true,
                "username"    to username,
                "user_id"     to userId,
                "role"        to (role ?: "staff")
            )
            if (restaurantId != null) claims["restaurant_id"] = restaurantId
            ResponseEntity.ok(ApiResponse.success(claims, "Token is valid"))
        } else {
            log.debug("Token validation failed")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("INVALID_TOKEN", "Token is invalid or expired")
            )
        }
    }
}
