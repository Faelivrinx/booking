package com.dominikdev.booking.identity.web

import com.dominikdev.booking.identity.domain.DuplicateUserException
import com.dominikdev.booking.identity.domain.IdentityException
import com.dominikdev.booking.identity.domain.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice(basePackages = ["com.dominikdev.booking.identity"])
class IdentityControllerAdvice {

    @ExceptionHandler(DuplicateUserException::class)
    fun handleDuplicateUserException(ex: DuplicateUserException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "User already exists",
                path = "/api/identity"
            )
        )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "User not found",
                path = "/api/identity"
            )
        )
    }

    @ExceptionHandler(IdentityException::class)
    fun handleIdentityException(ex: IdentityException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Identity operation failed",
                path = "/api/identity"
            )
        )
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)