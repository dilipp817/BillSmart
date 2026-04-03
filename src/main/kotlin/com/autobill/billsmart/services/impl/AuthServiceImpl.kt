package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.LoginRequest
import com.autobill.billsmart.dto.LoginResponse
import com.autobill.billsmart.dto.UserInfoResponse
import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.repositories.UserRepository
import com.autobill.billsmart.security.JwtTokenProvider
import com.autobill.billsmart.services.AuthService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Authentication Service Implementation
 * Handles user login and authentication
 */
@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : AuthService {

    private val log = LoggerFactory.getLogger(AuthServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun login(request: LoginRequest): LoginResponse {
        log.debug("Login attempt for username: {}", request.username)

        val user = userRepository.findByUsername(request.username)
            ?: throw AppException.ResourceNotFoundException("Invalid username or password")

        if (!user.isActive) {
            log.warn("Inactive user attempted login: {}", request.username)
            throw AppException.UnauthorizedException("Account is deactivated")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            log.warn("Invalid password for user: {}", request.username)
            throw AppException.UnauthorizedException("Invalid username or password")
        }

        val token = jwtTokenProvider.generateToken(user.username, user.id!!, user.role)
        val expiresIn = jwtTokenProvider.getExpirationTimeInSeconds()

        log.info("Login successful for user: {}", request.username)

        return LoginResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            role = user.role,
            token = token,
            expiresIn = expiresIn
        )
    }

    @Transactional(readOnly = true)
    override fun getCurrentUser(username: String): UserInfoResponse? {
        log.debug("Fetching current user: {}", username)

        val user = userRepository.findByUsername(username)
            ?: return null

        return UserInfoResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            role = user.role,
            isActive = user.isActive
        )
    }

    @Transactional(readOnly = true)
    override fun userExists(username: String): Boolean {
        return userRepository.findByUsername(username) != null
    }
}

