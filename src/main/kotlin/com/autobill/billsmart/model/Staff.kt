package com.autobill.billsmart.model

import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "staff")
open class Staff(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    open var id: Long? = null,

    @field:NotBlank
    @Column(name = "name", nullable = false)
    open var name: String = "",

    @field:NotBlank
    @Column(name = "gender", nullable = false)
    open var gender: String = "",

    @field:NotBlank
    @Column(name = "age", nullable = false)
    open var age: String = "",

    @field:NotBlank
    @Column(name = "salary", nullable = false)
    open var salary: String = "",

    @Embedded
    @field:Valid
    open var address: Address = Address()
)
