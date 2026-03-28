package com.autobill.billsmart.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

/**
 * JWT Token Provider
 * Generates, validates, and extracts information from JWT tokens
 */
@Component
class JwtTokenProvider {

    @Value("\${app.jwt.secret:your-secret-key-change-this-in-production}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration:86400000}") // 24 hours
    private val jwtExpirationMs: Long = 86400000

    /**
     * Generate JWT token for user
     */
    fun generateToken(username: String, userId: Long, role: String): String {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * Get username from JWT token
     */
    fun getUsernameFromToken(token: String): String? {
        return try {
            val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            claims.payload.subject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get user ID from JWT token
     */
    fun getUserIdFromToken(token: String): Long? {
        return try {
            val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            claims.payload.get("userId", Long::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get role from JWT token
     */
    fun getRoleFromToken(token: String): String? {
        return try {
            val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            claims.payload.get("role", String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validate JWT token
     */
    fun validateToken(token: String): Boolean {
        return try {
            val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get expiration time in seconds
     */
    fun getExpirationTimeInSeconds(): Long = jwtExpirationMs / 1000
}

