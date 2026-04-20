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
    @Mapping(target = "deleted", constant = "false")   // MapStruct strips 'is' prefix from Boolean — property is 'deleted' not 'isDeleted'
    @Mapping(target = "deletedAt", ignore = true)
    fun toFood(req: FoodRequest): Food

    @Mapping(source = "restaurant.restroId", target = "restaurantId")
    @Mapping(source = "available", target = "isAvailable")
    @Mapping(source = "vegetarian", target = "isVegetarian")
    @Mapping(source = "spicy", target = "isSpicy")
    fun toResponse(food: Food): FoodResponse
}
