package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.FoodRequest
import com.autobill.billsmart.dto.FoodResponse
import com.autobill.billsmart.model.Food
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface FoodMapper {
    @Mapping(target = "restaurant", ignore = true)
    fun toFood(req: FoodRequest): Food

    // map entity -> DTO with restaurantId extracted from restaurant.restroId
    @Mapping(source = "restaurant.restroId", target = "restaurantId")
    fun toResponse(food: Food): FoodResponse
}
