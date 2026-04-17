package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.model.Bill
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * BillMapper - Converts between Bill Entity and DTOs
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = org.mapstruct.ReportingPolicy.WARN
)
interface BillMapper {

    /**
     * Convert Bill Entity to BillResponse DTO
     * paidAmount and remainingAmount are intentionally unmapped here —
     * they are computed from payments by BillServiceImpl.enrichWithPaymentTotals() (B-NEW-1)
     */
    @Mapping(target = "restaurantName", source = "restaurant.outletName")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "restaurantId", source = "restaurant.restroId")
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    fun toResponse(bill: Bill): BillResponse

    /**
     * Convert list of Bills to BillResponse list
     */
    fun toResponses(bills: List<Bill>): List<BillResponse>

    /**
     * Convert Bill to BillListResponse (lightweight)
     */
    @Mapping(target = "restaurantName", source = "restaurant.outletName")
    @Mapping(target = "orderId", source = "order.id")
    fun toListResponse(bill: Bill): BillListResponse

    /**
     * Convert BillItem to BillItemResponse
     */
    @Mapping(target = "foodName", source = "food.name")
    @Mapping(target = "billId", source = "bill.id")
    @Mapping(target = "foodId", source = "food.id")
    fun toItemResponse(item: com.autobill.billsmart.model.BillItem): BillItemResponse

    /**
     * Convert list of BillItems to BillItemResponse list
     */
    fun toItemResponses(items: List<com.autobill.billsmart.model.BillItem>): List<BillItemResponse>
}

