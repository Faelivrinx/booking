package com.dominikdev.booking.business.staff

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@RestControllerAdvice(basePackages = ["com.dominikdev.booking.business.staff"])
class StaffControllerAdvice {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(StaffDomainException::class)
    fun handleStaffDomainException(
        ex: StaffDomainException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Staff domain error: ${ex.message}" }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Error in staff operation",
            path = request.getDescription(false).substringAfter("uri=")
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    data class ErrorResponse(
        val timestamp: LocalDateTime,
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )
}