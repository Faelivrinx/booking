package com.dominikdev.booking.appointment.api

import com.dominikdev.booking.appointment.domain.model.AppointmentException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@RestControllerAdvice(basePackages = ["com.dominikdev.booking.appointment.api"])
class AppointmentControllerAdvice {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(AppointmentException::class)
    fun handleAppointmentException(
        ex: AppointmentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Appointment error: ${ex.message}" }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Error in appointment operation",
            path = request.getDescription(false).substringAfter("uri=")
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(
        ex: DataIntegrityViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Data integrity violation: ${ex.message}" }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = "This time slot is no longer available. Please select another time slot.",
            path = request.getDescription(false).substringAfter("uri=")
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UnexpectedRollbackException::class)
    fun handleUnexpectedRollbackException(
        ex: UnexpectedRollbackException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected rollback exception: ${ex.message}" }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = "Unable to complete the booking. The time slot may no longer be available. Please try again.",
            path = request.getDescription(false).substringAfter("uri=")
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected error in appointment API: ${ex.message}" }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please try again later.",
            path = request.getDescription(false).substringAfter("uri=")
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    data class ErrorResponse(
        val timestamp: LocalDateTime,
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )
}