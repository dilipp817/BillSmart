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

    data class SeedUser(val username: String, val email: String, val rawPassword: String, val role: String)

    private val seedUsers = listOf(
        SeedUser("admin",  "admin@billsmart.com",  "admin123", "admin"),
        SeedUser("staff1", "staff1@billsmart.com", "staff123", "staff"),
        SeedUser("staff2", "staff2@billsmart.com", "staff123", "staff")
    )

    override fun run(args: ApplicationArguments) {
        seedUsers.forEach { seed ->
            if (userRepository.findByUsername(seed.username) == null) {
                userRepository.save(User(
                    username = seed.username,
                    email = seed.email,
                    password = passwordEncoder.encode(seed.rawPassword),
                    role = seed.role,
                    isActive = true
                ))
                log.info("Seeded user: {} (role: {})", seed.username, seed.role)
            } else {
                log.debug("User already exists, skipping: {}", seed.username)
            }
        }
    }
}
