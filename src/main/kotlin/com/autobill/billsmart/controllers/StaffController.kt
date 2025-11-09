package com.autobill.billsmart.controllers

import com.autobill.billsmart.dto.StaffRequest
import com.autobill.billsmart.model.Staff
import com.autobill.billsmart.services.StaffService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/create")
class StaffController(
    private val staffService: StaffService
) {

    @PostMapping("/user")
    fun createUser(@Valid @RequestBody req: StaffRequest): ResponseEntity<Staff> {
        val saved = staffService.createStaff(req)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }
}
