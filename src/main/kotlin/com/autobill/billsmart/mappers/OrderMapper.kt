package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.*
import com.autobill.billsmart.model.Food
import com.autobill.billsmart.model.Order
import com.autobill.billsmart.model.OrderItem
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * OrderMapper - Converts between Order Entity and DTOs
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = org.mapstruct.ReportingPolicy.WARN
)
interface OrderMapper {

    /**
     * Convert Order Entity to OrderResponse DTO
     */
    @Mapping(target = "restaurantId", source = "restaurant.restroId")
    @Mapping(target = "tableId", source = "table.id")
    @Mapping(target = "tableNumber", source = "table.tableNumber")
    fun toResponse(order: Order): OrderResponse

    /**
     * Convert list of Orders to OrderResponse list
     */
    fun toResponses(orders: List<Order>): List<OrderResponse>

    /**
     * Convert Order to OrderSummaryResponse
     */
    @Mapping(target = "tableNumber", source = "table.tableNumber")
    @Mapping(target = "itemCount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    fun toSummaryResponse(order: Order): OrderSummaryResponse?

    /**
     * Convert OrderItem to OrderItemResponse
     */
    @Suppress("unused")
    @Mapping(target = "foodId", source = "food.id")
    @Mapping(target = "foodName", source = "food.name")
    fun toItemResponse(item: OrderItem): OrderItemResponse

    /**
     * Convert list of OrderItems to OrderItemResponse list
     */
    @Suppress("unused")
    fun toItemResponses(items: List<OrderItem>): List<OrderItemResponse>
}

/**
 * Helper function to create OrderItem from OrderItemRequest and related entities
 * This is outside the mapper interface to avoid MapStruct conflicts
 */
fun createOrderItemFromRequest(
    request: OrderItemRequest,
    food: Food,
    order: Order
): OrderItem {
    return OrderItem().apply {
        this.order = order
        this.food = food
        this.quantity = request.quantity
        this.unitPrice = food.price?.toBigDecimal() ?: java.math.BigDecimal.ZERO
        this.specialRequests = request.specialRequests
        this.itemStatus = "PENDING"
        this.calculateSubtotal()
    }
}

