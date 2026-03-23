package com.autobill.billsmart.adapters.persistence

import com.autobill.billsmart.exception.AppException
import com.autobill.billsmart.model.Staff
import com.autobill.billsmart.ports.StaffRepositoryPort
import com.autobill.billsmart.repositories.StaffRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository

/**
 * Adapter implementing the persistence port for Staff using Spring Data JPA.
 *
 * Best practices applied:
 * - Use `@Repository` to indicate a persistence component and enable Spring's
 *   exception translation for JPA exceptions.
 * - Keep transactions at the service/use-case boundary (do not start transactions here).
 * - Add structured logging via SLF4J instead of println.
 * - Handle DataAccessExceptions and convert to domain exceptions.
 */
@Repository
class StaffRepositoryAdapter(
    private val staffRepository: StaffRepository
) : StaffRepositoryPort {

    private val log = LoggerFactory.getLogger(StaffRepositoryAdapter::class.java)

    override fun save(staff: Staff): Staff {
        return try {
            val saved = staffRepository.save(staff)
            log.debug("Saved staff with id={}", saved.id)
            saved
        } catch (ex: DataAccessException) {
            log.error("Database error saving staff", ex)
            throw AppException.PersistenceException("Failed to save staff", ex)
        }
    }

    override fun findById(id: Long): Staff? {
        return try {
            val found = staffRepository.findById(id).orElse(null)
            if (found == null) {
                log.debug("Staff not found for id={}", id)
            } else {
                log.debug("Found staff id={}", found.id)
            }
            found
        } catch (ex: DataAccessException) {
            log.error("Database error finding staff id={}", id, ex)
            throw AppException.PersistenceException("Failed to find staff", ex)
        }
    }
}

