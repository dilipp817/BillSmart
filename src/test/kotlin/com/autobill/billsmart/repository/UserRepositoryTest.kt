package com.autobill.billsmart.repository

import com.autobill.billsmart.model.User
import com.autobill.billsmart.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository
) {

    @Test
    fun `save and find user`() {
        val u = User(username = "testuser", email = "test@example.com")
        val saved = userRepository.save(u)
        val found = userRepository.findById(saved.id!!).orElseThrow()
        assertEquals("testuser", found.username)
        assertEquals("test@example.com", found.email)
    }
}

