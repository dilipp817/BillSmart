package com.autobill.billsmart.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class RestExceptionHandler {

    data class ErrorResponse(val status: Int, val error: String, val message: String?)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val msg = "Invalid value for parameter '${ex.name}': '${ex.value}'. Expected type: ${ex.requiredType?.simpleName}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", msg))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed", msg))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.message))
    }
}

