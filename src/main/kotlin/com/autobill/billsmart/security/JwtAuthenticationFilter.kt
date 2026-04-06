package com.autobill.billsmart.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT Authentication Filter
 *
 * Intercepts every request, extracts the Bearer token from the Authorization header,
 * validates it via [JwtTokenProvider], and populates the [SecurityContextHolder]
 * so that Spring Security's `anyRequest().authenticated()` rule is satisfied.
 *
 * This filter is stateless — it does not create HTTP sessions.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractBearerToken(request)

        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                val username = jwtTokenProvider.getUsernameFromToken(token)
                val role     = jwtTokenProvider.getRoleFromToken(token) ?: "staff"

                if (username != null) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
                    val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
                    SecurityContextHolder.getContext().authentication = auth
                    log.debug("JWT authenticated user '{}' with role '{}'", username, role)
                }
            } else {
                log.debug("Invalid or expired JWT token on request to {}", request.requestURI)
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extracts the raw JWT from the `Authorization: Bearer <token>` header.
     * Returns null if the header is absent or malformed.
     */
    private fun extractBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        return header.substring(7).trim().takeIf { it.isNotEmpty() }
    }
}

