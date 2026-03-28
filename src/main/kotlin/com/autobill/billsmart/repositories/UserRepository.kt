package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    fun findByUsername(username: String): User?

    /**
     * Find user by email
     */
    fun findByEmail(email: String): User?
}
