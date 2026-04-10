package com.autobill.billsmart.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Token Provider
 *
 * Generates, validates and extracts claims from JWT tokens using JJWT 0.12.x API.
 * Algorithm is HS256 (inferred automatically from the 32+ byte secret key).
 * The signing key is initialised once at startup via [@PostConstruct].
 */
@Component
class JwtTokenProvider {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration:86400000}") // 24 hours in ms
    private val jwtExpirationMs: Long = 86400000

    /** HMAC-SHA signing key — built once after Spring injects [jwtSecret]. */
    private lateinit var signingKey: SecretKey

    @PostConstruct
    fun init() {
        signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray(Charsets.UTF_8))
    }

    /** Generate a signed JWT for the given user. */
    fun generateToken(username: String, userId: Long, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(username)
            .claim("userId", userId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + jwtExpirationMs))
            .signWith(signingKey)          // HS256 inferred from 32-byte key
            .compact()
    }

    /** Extract username (subject) from token; returns null if invalid/expired. */
    fun getUsernameFromToken(token: String): String? = runCatching {
        parseClaims(token).subject
    }.getOrNull()

    /** Extract userId claim from token; returns null if invalid/expired.
     *  JJWT deserialises small JSON numbers as Integer, not Long, so we
     *  normalise any Number subtype to Long to avoid a silent null result. */
    fun getUserIdFromToken(token: String): Long? = runCatching {
        val raw = parseClaims(token)["userId"] ?: return@runCatching null
        (raw as? Number)?.toLong()
    }.getOrNull()

    /** Extract role claim from token; returns null if invalid/expired. */
    fun getRoleFromToken(token: String): String? = runCatching {
        parseClaims(token).get("role", String::class.java)
    }.getOrNull()

    /** Returns true iff the token signature is valid and it has not expired. */
    fun validateToken(token: String): Boolean = runCatching {
        parseClaims(token)
        true
    }.getOrDefault(false)

    /** Expiration time in seconds (for the [expiresIn] response field). */
    fun getExpirationTimeInSeconds(): Long = jwtExpirationMs / 1000

    // ── private ──────────────────────────────────────────────────────────────

    private fun parseClaims(token: String) =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
