package com.autobill.billsmart.controllers

import com.autobill.billsmart.dto.FoodRequest
import com.autobill.billsmart.dto.FoodResponse
import com.autobill.billsmart.services.FoodService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/restaurants/{restroId}/foods")
class FoodController(
    private val foodService: FoodService
) {

    @PostMapping
    fun createFood(@PathVariable("restroId") restroId: Long, @Valid @RequestBody req: FoodRequest): ResponseEntity<FoodResponse> {
        val saved = foodService.createFood(restroId, req)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }

    @GetMapping
    fun getAllFoods(@PathVariable("restroId") restroId: Long): ResponseEntity<List<FoodResponse>> {
        val foods = foodService.getAllFoods(restroId)
        return ResponseEntity.ok(foods)
    }

    @GetMapping("/{id}")
    fun getFood(@PathVariable("restroId") restroId: Long, @PathVariable("id") id: Long): ResponseEntity<FoodResponse> {
        val f = foodService.getFood(id) ?: return ResponseEntity.notFound().build()
        // Optional: verify the food belongs to the requested restaurant
        if (f.restroId != restroId) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        return ResponseEntity.ok(f)
    }
}
