package com.autobill.billsmart.config

import com.autobill.billsmart.model.User
import com.autobill.billsmart.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev")
class DataInitializer {
    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun init(repository: UserRepository) = CommandLineRunner {
        repository.save(User(username = "admin", email = "admin@example.com"))
        val users = repository.findAll()
        log.info("Users in DB at startup: {}", users)
    }
}
