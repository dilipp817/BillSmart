package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.AddressRequest
import com.autobill.billsmart.dto.StaffRequest
import com.autobill.billsmart.model.Address
import com.autobill.billsmart.model.Staff
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface StaffMapper {
    fun toAddress(req: AddressRequest): Address
    fun toStaff(req: StaffRequest): Staff
}
