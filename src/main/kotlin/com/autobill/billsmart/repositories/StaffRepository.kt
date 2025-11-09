package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Staff
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffRepository : JpaRepository<Staff, Long>

