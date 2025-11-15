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

    // map entity -> DTO with restroId extracted
    @Mapping(source = "restaurant.restroId", target = "restroId")
    fun toResponse(food: Food): FoodResponse
}
