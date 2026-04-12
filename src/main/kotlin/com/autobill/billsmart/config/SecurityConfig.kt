package com.autobill.billsmart.config

import com.autobill.billsmart.security.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

// Security Configuration — stateless JWT-based security, no sessions, no HTTP Basic, no form login.
// Roles extracted from JWT by JwtAuthenticationFilter are enforced here via requestMatchers.
// Role hierarchy: ADMIN > MANAGER > STAFF (each role grants all lower-role permissions).
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    // Public endpoints: /actuator/health, /actuator/info, /api/v1/auth/...
    // All other endpoints require a valid JWT Bearer token AND the appropriate role.
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .anonymous { it.disable() }  // disable so missing-token → 401, not 403
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                // Return 401 when no/invalid token is provided
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()

                    // ── Restaurant management — ADMIN only ──────────────────────────────────
                    // GET /restaurants lists ALL tenants (cross-tenant); POST creates a new one.
                    // Neither is a POS operation — restrict to admin to prevent misuse.
                    .requestMatchers(HttpMethod.GET, "/api/v1/restaurants").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/restaurants").hasRole("ADMIN")

                    // ── Category mutations — ADMIN only ─────────────────────────────────────
                    // GET /categories is open to any authenticated role (staff needs it for menu).
                    // Write operations are admin-console-only; mobile POS never creates categories.
                    .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")

                    // ── Food mutations — ADMIN only ──────────────────────────────────────────
                    // Creating/editing menu items is an admin task; staff only reads the menu.
                    .requestMatchers(HttpMethod.POST, "/api/v1/foods/**").hasRole("ADMIN")

                    // ── Table mutations — MANAGER or ADMIN ──────────────────────────────────
                    // Staff reads tables; managers/admins create, rename, or remove them.
                    .requestMatchers(HttpMethod.POST, "/api/v1/restaurants/*/tables").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/restaurants/*/tables/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurants/*/tables/**").hasAnyRole("ADMIN", "MANAGER")

                    // ── Order cancellation — MANAGER or ADMIN ───────────────────────────────
                    // Per integration guide: "manager — Can cancel orders". Staff can add items
                    // and update item statuses but cannot cancel an entire order.
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/restaurants/*/orders/*").hasAnyRole("ADMIN", "MANAGER")

                    // Everything else (order CRUD, payments, bills, food reads) → any authenticated role
                    .anyRequest().authenticated()
            }
            // Validate JWT before Spring's UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    // BCrypt password encoder — used for login verification and DataInitializer.
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
