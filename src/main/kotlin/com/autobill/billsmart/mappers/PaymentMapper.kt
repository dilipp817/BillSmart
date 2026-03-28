package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.model.Payment
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * PaymentMapper - Converts between Payment Entity and DTOs
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = org.mapstruct.ReportingPolicy.WARN
)
interface PaymentMapper {

    /**
     * Convert Payment Entity to PaymentResponse DTO
     */
    @Mapping(target = "billId", source = "bill.id")
    @Mapping(target = "orderId", source = "order.id")
    fun toResponse(payment: Payment): PaymentResponse

    /**
     * Convert list of Payments to PaymentResponse list
     */
    fun toResponses(payments: List<Payment>): List<PaymentResponse>

    /**
     * Convert Payment to PaymentListResponse (lightweight)
     */
    @Mapping(target = "orderId", source = "order.id")
    fun toListResponse(payment: Payment): PaymentListResponse
}

