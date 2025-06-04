package com.dominikdev.booking.appointment.api

import com.dominikdev.booking.appointment.application.AppointmentDTO
import com.dominikdev.booking.appointment.application.AppointmentFacade
import com.dominikdev.booking.appointment.application.BookAppointmentRequest
import com.dominikdev.booking.appointment.application.CancelAppointmentRequest
import com.dominikdev.booking.appointment.application.ClientAppointmentsResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

/**
 * REST controller for client appointment operations
 */
@RestController
@RequestMapping("/appointments")
class ClientAppointmentController(private val appointmentFacade: AppointmentFacade) {

    /**
     * Books a new appointment
     */
    @PostMapping
    fun bookAppointment(
        @RequestBody request: BookAppointmentRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<AppointmentDTO> {
        val clientId = UUID.fromString(jwt.subject)
        val appointment = appointmentFacade.bookAppointment(request, clientId)
        return ResponseEntity(appointment, HttpStatus.CREATED)
    }

    /**
     * Gets all client's appointments with filtering options
     */
    @GetMapping
    fun getClientAppointments(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<List<AppointmentDTO>> {
        val clientId = UUID.fromString(jwt.subject)

        val appointments = if (startDate != null && endDate != null) {
            appointmentFacade.getClientAppointmentsByDateRange(clientId, startDate, endDate)
        } else {
            appointmentFacade.getClientAppointments(clientId)
        }

        return ResponseEntity.ok(appointments)
    }

    /**
     * Gets client's upcoming appointments
     */
    @GetMapping("/upcoming")
    fun getUpcomingAppointments(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<AppointmentDTO>> {
        val clientId = UUID.fromString(jwt.subject)
        val appointments = appointmentFacade.getClientUpcomingAppointments(clientId)
        return ResponseEntity.ok(appointments)
    }

    /**
     * Gets client's appointments in a client-friendly view
     */
    @GetMapping("/view")
    fun getAppointmentsView(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ClientAppointmentsResponse> {
        val clientId = UUID.fromString(jwt.subject)
        val view = appointmentFacade.createClientAppointmentsView(clientId)
        return ResponseEntity.ok(view)
    }

    /**
     * Gets a specific appointment by ID
     */
    @GetMapping("/{appointmentId}")
    fun getAppointment(
        @PathVariable appointmentId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<AppointmentDTO> {
        val clientId = UUID.fromString(jwt.subject)
        val appointment = appointmentFacade.getAppointment(
            UUID.fromString(appointmentId), clientId
        )
        return ResponseEntity.ok(appointment)
    }

    /**
     * Cancels an appointment
     */
    @PutMapping("/{appointmentId}/cancel")
    fun cancelAppointment(
        @PathVariable appointmentId: String,
        @RequestBody request: CancelAppointmentRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<AppointmentDTO> {
        val clientId = UUID.fromString(jwt.subject)
        val appointment = appointmentFacade.cancelAppointment(
            UUID.fromString(appointmentId), clientId, request.reason
        )
        return ResponseEntity.ok(appointment)
    }
}