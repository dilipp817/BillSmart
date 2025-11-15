package com.autobill.billsmart.controllers

import com.autobill.billsmart.dto.RestaurantRequest
import com.autobill.billsmart.model.Restaurant
import com.autobill.billsmart.services.RestaurantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService
) {

    @PostMapping("/create")
    fun createRestaurant(@Valid @RequestBody req: RestaurantRequest): ResponseEntity<Restaurant> {
        val saved = restaurantService.createRestaurant(req)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }


    @GetMapping("get/{id}")
    fun getRestaurant(@PathVariable id: Long): ResponseEntity<Restaurant> {
        val restaurant = restaurantService.getRestaurant(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(restaurant)
    }
}

