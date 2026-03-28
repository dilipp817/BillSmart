package com.autobill.billsmart.controller

import com.autobill.billsmart.dto.ApiResponse
import com.autobill.billsmart.dto.LoginRequest
import com.autobill.billsmart.dto.LoginResponse
import com.autobill.billsmart.dto.UserInfoResponse
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
    private val authService: AuthService
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
     * @param username Username extracted from JWT token
     * @return UserInfoResponse with user details
     */
    @GetMapping("/me")
    fun getCurrentUser(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<ApiResponse<UserInfoResponse>> {
        log.debug("Fetching current user info")

        // Extract username from token (in real implementation, use JWT extraction)
        val username = extractUsernameFromToken(authHeader)
            ?: return ResponseEntity(
                ApiResponse.error<UserInfoResponse>(
                    "UNAUTHORIZED",
                    "No valid token provided"
                ),
                HttpStatus.UNAUTHORIZED
            )

        val userInfo = authService.getCurrentUser(username)
            ?: return ResponseEntity(
                ApiResponse.error<UserInfoResponse>(
                    "NOT_FOUND",
                    "User not found"
                ),
                HttpStatus.NOT_FOUND
            )

        return ResponseEntity.ok(ApiResponse.success(userInfo, "User info retrieved"))
    }

    /**
     * POST /api/v1/auth/validate
     * Validate JWT token
     *
     * @param token JWT token to validate
     * @return Success if token is valid
     */
    @PostMapping("/validate")
    fun validateToken(@RequestParam token: String): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.debug("Validating token")

        val isValid = token.isNotEmpty() // In real implementation, validate with JwtTokenProvider

        return if (isValid) {
            ResponseEntity.ok(
                ApiResponse.success(
                    mapOf("valid" to true),
                    "Token is valid"
                )
            )
        } else {
            ResponseEntity(
                ApiResponse.error<Map<String, Any>>(
                    "INVALID_TOKEN",
                    "Token is invalid or expired"
                ),
                HttpStatus.UNAUTHORIZED
            )
        }
    }

    /**
     * Helper function to extract username from Authorization header
     * In production, use JwtTokenProvider
     */
    private fun extractUsernameFromToken(authHeader: String?): String? {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null
        }

        val token = authHeader.substring(7)
        // In real implementation: return jwtTokenProvider.getUsernameFromToken(token)
        return null
    }
}

