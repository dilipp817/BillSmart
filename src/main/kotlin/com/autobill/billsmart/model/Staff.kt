package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

@Entity
@jakarta.persistence.Table(name = "staff")
class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @field:NotBlank
    @Column(name = "name", nullable = false)
    var name: String = ""

    @field:NotBlank
    @Column(name = "gender", nullable = false)
    var gender: String = ""

    @field:NotBlank
    @Column(name = "age", nullable = false)
    var age: String = ""

    @field:NotBlank
    @Column(name = "salary", nullable = false)
    var salary: String = ""

    @Embedded
    @field:Valid
    var address: Address = Address()
}
