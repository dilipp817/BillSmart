package com.autobill.billsmart.services

import com.autobill.billsmart.dto.LoginRequest
import com.autobill.billsmart.dto.LoginResponse
import com.autobill.billsmart.dto.UserInfoResponse
import com.autobill.billsmart.model.User

/**
 * Authentication Service Interface
 */
interface AuthService {

    /**
     * Login user with username and password
     */
    fun login(request: LoginRequest): LoginResponse

    /**
     * Get current user info
     */
    fun getCurrentUser(username: String): UserInfoResponse?

    /**
     * Validate if user exists
     */
    fun userExists(username: String): Boolean
}

