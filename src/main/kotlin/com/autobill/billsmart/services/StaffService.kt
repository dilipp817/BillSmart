package com.autobill.billsmart.services

import com.autobill.billsmart.dto.StaffRequest
import com.autobill.billsmart.model.Staff

interface StaffService {
    fun createStaff(req: StaffRequest): Staff
}

