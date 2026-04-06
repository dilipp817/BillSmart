package com.autobill.billsmart.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * Handles low-level Spring MVC type-mismatch errors that are not covered by
 * [com.autobill.billsmart.controller.GlobalExceptionHandler].
 *
 * NOTE: MethodArgumentNotValidException and IllegalArgumentException are
 * intentionally NOT handled here — they are handled by GlobalExceptionHandler
 * to keep the error response format consistent (ApiResponse envelope).
 */
@ControllerAdvice
class RestExceptionHandler {

    data class ErrorResponse(val status: Int, val error: String, val message: String?)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val msg = "Invalid value for parameter '${ex.name}': '${ex.value}'. " +
                  "Expected type: ${ex.requiredType?.simpleName}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", msg))
    }
}
