package com.autobill.billsmart.mappers

import com.autobill.billsmart.dto.TableAvailabilityResponse
import com.autobill.billsmart.dto.TableResponse
import com.autobill.billsmart.dto.TableRequest
import com.autobill.billsmart.model.Restaurant
import com.autobill.billsmart.model.Table
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * TableMapper - Converts between Table Entity and DTOs
 *
 * Design Patterns:
 * - Mapper Pattern: Centralized conversion logic
 * - MapStruct: Compile-time code generation for performance
 * - Dependency Injection: Via Spring component model
 *
 * Benefits:
 * - Type-safe conversions
 * - Compile-time verification
 * - Zero-reflection performance
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = org.mapstruct.ReportingPolicy.WARN
)
interface TableMapper {

    /**
     * Convert TableRequest and Restaurant to Table Entity
     * Maps DTO input to entity for persistence
     *
     * @param request the table request DTO
     * @param restaurant the restaurant entity
     * @return table entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", source = "restaurant")
    @Mapping(target = "currentOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastOccupiedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    fun toEntity(request: TableRequest, restaurant: Restaurant): Table

    /**
     * Convert Table Entity to TableResponse DTO
     * Maps entity to API response format
     *
     * @param table the table entity
     * @return table response DTO
     */
    @Mapping(target = "restaurantId", source = "restaurant.restroId")
    @Mapping(target = "currentOrderId", source = "currentOrder.id")
    fun toResponse(table: Table): TableResponse

    /**
     * Convert Table Entity to TableAvailabilityResponse DTO
     * Minimal response for availability checks
     *
     * @param table the table entity
     * @return availability response DTO
     */
    fun toAvailabilityResponse(table: Table): TableAvailabilityResponse

    /**
     * Convert list of Table Entities to TableResponse DTOs
     * Batch conversion for list operations
     *
     * @param tables list of table entities
     * @return list of table response DTOs
     */
    fun toResponses(tables: List<Table>): List<TableResponse>

    /**
     * Convert list of Table Entities to availability responses
     *
     * @param tables list of table entities
     * @return list of availability response DTOs
     */
    fun toAvailabilityResponses(tables: List<Table>): List<TableAvailabilityResponse>
}

