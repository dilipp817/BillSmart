package com.autobill.billsmart.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank

@Embeddable
class Address(
    @field:NotBlank
    @Column(name = "building", nullable = false)
    var building: String = "",

    @field:NotBlank
    @Column(name = "street", nullable = false)
    var street: String = "",

    @field:NotBlank
    @Column(name = "store_location", nullable = false)
    var storelocation: String = "",

    @field:NotBlank
    @Column(name = "zip_code", nullable = false)
    var zipCode: String = ""
)
