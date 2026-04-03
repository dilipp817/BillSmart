package com.autobill.billsmart.services.impl

import com.autobill.billsmart.dto.StaffRequest
import com.autobill.billsmart.model.Staff
import com.autobill.billsmart.mappers.StaffMapper
import com.autobill.billsmart.repositories.StaffRepository
import com.autobill.billsmart.services.StaffService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val staffMapper: StaffMapper
) : StaffService {

    @Transactional
    override fun createStaff(req: StaffRequest): Staff {
        val staff = staffMapper.toStaff(req)
        return staffRepository.save(staff)
    }
}
