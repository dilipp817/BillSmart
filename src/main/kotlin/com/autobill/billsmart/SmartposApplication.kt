package com.autobill.billsmart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SmartposApplication

fun main(args: Array<String>) {
	runApplication<SmartposApplication>(*args)
	println("Smart POS Application started successfully.")

}
