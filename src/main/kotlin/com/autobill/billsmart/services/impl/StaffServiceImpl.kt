package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.StaffRequest
import com.autobill.billsmart.model.Staff
import com.autobill.billsmart.mappers.StaffMapper
import com.autobill.billsmart.ports.StaffRepositoryPort
import com.autobill.billsmart.services.StaffService
import org.springframework.stereotype.Service

@Service
class StaffServiceImpl(
    private val staffRepositoryPort: StaffRepositoryPort,
    private val staffMapper: StaffMapper
) : StaffService {

    override fun createStaff(req: StaffRequest): Staff {
        val staff = staffMapper.toStaff(req)
        return staffRepositoryPort.save(staff)
    }
}
