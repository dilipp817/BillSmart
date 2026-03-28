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
    @Mapping(target = "restaurantId", source = "restaurant.id")
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
    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "tableNumber", source = "table.tableNumber")
    @Mapping(target = "itemCount", source = "items.size")
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

    /**
     * Create OrderItem from OrderItemRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "food", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "itemStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    fun toOrderItem(request: OrderItemRequest, food: Food): OrderItem

    /**
     * Map OrderItemRequest with specific food
     */
    fun toOrderItem(request: OrderItemRequest, food: Food, order: Order): OrderItem {
        val item = toOrderItem(request, food)
        item.order = order
        item.unitPrice = food.price?.toBigDecimal() ?: java.math.BigDecimal.ZERO
        item.calculateSubtotal()
        return item
    }
}

