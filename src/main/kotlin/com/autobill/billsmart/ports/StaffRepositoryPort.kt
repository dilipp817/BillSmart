package com.autobill.billsmart.ports

import com.autobill.billsmart.model.Staff

interface StaffRepositoryPort {
    fun save(staff: Staff): Staff
    fun findById(id: Long): Staff?
}

