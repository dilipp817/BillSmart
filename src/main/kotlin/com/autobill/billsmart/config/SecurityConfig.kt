package com.autobill.billsmart.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Security Configuration
 * Provides beans for password encoding and security
 */
@Configuration
class SecurityConfig {

    /**
     * Password encoder bean
     * Uses BCrypt for secure password hashing
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

