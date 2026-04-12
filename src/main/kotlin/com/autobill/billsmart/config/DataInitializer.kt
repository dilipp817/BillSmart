package com.autobill.billsmart.config

import com.autobill.billsmart.model.User
import com.autobill.billsmart.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * DataInitializer — seeds default test users on startup (idempotent).
 *
 * ⚠️  Restricted to `dev` and `uat` profiles only.
 *     Production uses real user management — test accounts must NOT exist there.
 *
 * TEST CREDENTIALS:
 *   admin  / admin123  (role: admin)
 *   staff1 / staff123  (role: staff)
 *   staff2 / staff123  (role: staff)
 */
@Component
@Profile("dev", "uat")
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    data class SeedUser(
        val username: String,
        val email: String,
        val rawPassword: String,
        val role: String,
        /** Which outlet this account belongs to. null = super_admin spanning all outlets. */
        val restaurantId: Long? = null
    )

    private val seedUsers = listOf(
        // restaurant_id=1 → Spice Garden Bangalore (first seeded restaurant)
        SeedUser("admin",  "admin@billsmart.com",  "admin123", "admin", restaurantId = 1L),
        SeedUser("staff1", "staff1@billsmart.com", "staff123", "staff", restaurantId = 1L),
        // restaurant_id=2 → The Burger House Mumbai
        SeedUser("staff2", "staff2@billsmart.com", "staff123", "staff", restaurantId = 2L)
    )

    override fun run(args: ApplicationArguments) {
        seedUsers.forEach { seed ->
            if (userRepository.findByUsername(seed.username) == null) {
                userRepository.save(User(
                    username     = seed.username,
                    email        = seed.email,
                    password     = passwordEncoder.encode(seed.rawPassword),
                    role         = seed.role,
                    isActive     = true,
                    restaurantId = seed.restaurantId
                ))
                log.info("Seeded user: {} (role: {})", seed.username, seed.role)
            } else {
                log.debug("User already exists, skipping: {}", seed.username)
            }
        }
    }
}
