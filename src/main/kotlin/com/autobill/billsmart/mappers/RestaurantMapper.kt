package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.model.Restaurant
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface RestaurantMapper {
    fun toRestaurant(req: RestaurantRequest): Restaurant
}

