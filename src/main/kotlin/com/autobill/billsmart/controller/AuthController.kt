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
     * @param token JWT token to validate
     * @return Success if token is valid
     */
    @PostMapping("/validate")
    fun validateToken(@RequestParam token: String): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.debug("Validating token")

        val username = jwtTokenProvider.getUsernameFromToken(token)
        val userId = jwtTokenProvider.getUserIdFromToken(token)
        val role = jwtTokenProvider.getRoleFromToken(token)

        return if (username != null && userId != null) {
            ResponseEntity.ok(
                ApiResponse.success(
                    mapOf("valid" to true, "username" to username, "userId" to userId, "role" to (role ?: "staff")),
                    "Token is valid"
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("INVALID_TOKEN", "Token is invalid or expired")
            )
        }
    }
}
