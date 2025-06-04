package com.dominikdev.booking.appointment.api

import com.dominikdev.booking.appointment.application.*
import com.dominikdev.booking.appointment.domain.model.AppointmentException
import com.dominikdev.booking.shared.infrastructure.security.SecurityContextUtils
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/staff/appointments")
@PreAuthorize("hasRole('STAFF_MEMBER') or hasRole('BUSINESS_OWNER')")
class StaffAppointmentController(
    private val appointmentFacade: AppointmentFacade,
    private val appointmentService: AppointmentService,
    private val securityContextUtils: SecurityContextUtils
) {

    @GetMapping
    fun getStaffAppointments(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<StaffAppointmentsResponse> {
        val staffId = extractStaffId(jwt)

        val appointments = when {
            date != null -> appointmentService.getStaffAppointmentsForDate(staffId, date)
            startDate != null && endDate != null -> appointmentService.getStaffAppointmentsForDateRange(staffId, startDate, endDate)
            else -> appointmentService.getAllStaffAppointments(staffId)
        }

        val grouped = groupAppointmentsByDate(appointments)
        return ResponseEntity.ok(StaffAppointmentsResponse(
            appointments = appointments,
            groupedByDate = grouped,
            totalCount = appointments.size
        ))
    }

    /**
     * Gets upcoming appointments for the staff member
     */
    @GetMapping("/upcoming")
    fun getUpcomingAppointments(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<AppointmentDTO>> {
        val staffId = extractStaffId(jwt)
        val appointments = appointmentService.getStaffUpcomingAppointments(staffId)
        return ResponseEntity.ok(appointments)
    }

    /**
     * Gets today's appointments for the staff member
     */
    @GetMapping("/today")
    fun getTodayAppointments(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<AppointmentDTO>> {
        val staffId = extractStaffId(jwt)
        val appointments = appointmentService.getStaffAppointmentsForDate(staffId, LocalDate.now())
        return ResponseEntity.ok(appointments)
    }

    /**
     * Gets a specific appointment by ID
     * Verifies the staff member has access to this appointment
     */
    @GetMapping("/{appointmentId}")
    fun getAppointment(
        @PathVariable appointmentId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<AppointmentDTO> {
        val staffId = extractStaffId(jwt)
        val appointmentUuid = UUID.fromString(appointmentId)

        val appointment = appointmentService.getAppointmentById(appointmentUuid)
            ?: throw AppointmentException("Appointment not found")

        // Verify staff member has access to this appointment
        if (appointment.staffId != staffId) {
            throw AppointmentException("Access denied to this appointment")
        }

        return ResponseEntity.ok(appointment)
    }


    /**
     * Cancels an appointment
     * Staff can only cancel their own future appointments
     */
    @DeleteMapping("/{appointmentId}")
    fun cancelAppointment(
        @PathVariable appointmentId: String,
        @RequestBody(required = false) request: StaffCancelRequest?,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<CancelResponse> {
        val staffId = extractStaffId(jwt)
        val appointmentUuid = UUID.fromString(appointmentId)

        // Get the appointment first to verify ownership
        val appointment = appointmentService.getAppointmentById(appointmentUuid)
            ?: throw AppointmentException("Appointment not found")

        // Verify staff member owns this appointment
        if (appointment.staffId != staffId) {
            throw AppointmentException("You can only cancel your own appointments")
        }

        // Verify appointment is in the future
        if (appointment.date.isBefore(LocalDate.now())) {
            throw AppointmentException("Cannot cancel past appointments")
        }

        // Cancel the appointment
        val cancelledAppointment = appointmentService.staffCancelAppointment(
            StaffCancelAppointmentCommand(
                appointmentId = appointmentUuid,
                staffId = staffId,
                reason = request?.reason ?: "Cancelled by staff"
            )
        )

        return ResponseEntity.ok(CancelResponse(
            success = true,
            message = "Appointment cancelled successfully",
            appointment = cancelledAppointment
        ))
    }

    /**
     * Marks a client as no-show
     */
    @PutMapping("/{appointmentId}/no-show")
    fun markNoShow(
        @PathVariable appointmentId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<AppointmentDTO> {
        val staffId = extractStaffId(jwt)
        val appointmentUuid = UUID.fromString(appointmentId)

        // Verify ownership
        val appointment = appointmentService.getAppointmentById(appointmentUuid)
            ?: throw AppointmentException("Appointment not found")

        if (appointment.staffId != staffId) {
            throw AppointmentException("You can only mark no-show for your own appointments")
        }

        val noShow = appointmentFacade.markClientAsNoShow(appointmentUuid)
        return ResponseEntity.ok(noShow)
    }

    /**
     * For this MVP, we'll use the subject (user ID) as staff ID
     * In production, you might need to look up the staff ID from the user ID
     */
    private fun extractStaffId(jwt: Jwt): UUID {
        return UUID.fromString(jwt.subject)
    }

    /**
     * Groups appointments by date for easier display
     */
    private fun groupAppointmentsByDate(appointments: List<AppointmentDTO>): Map<LocalDate, List<AppointmentDTO>> {
        return appointments.groupBy { it.date }
            .toSortedMap()
    }

}

/**
 * Request for staff cancellation with reason
 */
data class StaffCancelRequest(
    val reason: String? = null
)

/**
 * Response for cancellation
 */
data class CancelResponse(
    val success: Boolean,
    val message: String,
    val appointment: AppointmentDTO
)

/**
 * Response for staff appointments with grouping
 */
data class StaffAppointmentsResponse(
    val appointments: List<AppointmentDTO>,
    val groupedByDate: Map<LocalDate, List<AppointmentDTO>>,
    val totalCount: Int
)