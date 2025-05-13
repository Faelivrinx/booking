package com.dominikdev.booking.appointment.application

import com.dominikdev.booking.appointment.domain.model.AppointmentException
import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Facade for the appointment system
 * Acts as a simplified interface for the appointment domain
 */
@Component
class AppointmentFacade(
    private val appointmentService: AppointmentService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Books a new appointment for a client
     */
    fun bookAppointment(request: BookAppointmentRequest, clientId: UUID): AppointmentDTO {
        logger.info { "Processing appointment booking for client $clientId" }

        val command = BookAppointmentCommand(
            businessId = request.businessId,
            clientId = clientId,
            staffId = request.staffId,
            serviceId = request.serviceId,
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            notes = request.notes
        )

        return appointmentService.bookAppointment(command)
    }

    /**
     * Gets all appointments for a client
     */
    fun getClientAppointments(clientId: UUID): List<AppointmentDTO> {
        return appointmentService.getClientAppointments(clientId)
    }

    /**
     * Gets a client's upcoming appointments
     */
    fun getClientUpcomingAppointments(clientId: UUID): List<AppointmentDTO> {
        return appointmentService.getClientUpcomingAppointments(clientId)
    }

    /**
     * Gets a client's appointments for a specific date range
     */
    fun getClientAppointmentsByDateRange(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AppointmentDTO> {
        return appointmentService.getClientAppointmentsByDateRange(clientId, startDate, endDate)
    }

    /**
     * Gets full details of a specific appointment
     */
    fun getAppointment(appointmentId: UUID, clientId: UUID): AppointmentDTO {
        val appointment = appointmentService.getAppointmentById(appointmentId)
            ?: throw AppointmentException("Appointment not found")

        // Security check - ensure the client owns this appointment
        if (appointment.clientId != clientId) {
            throw AppointmentException("Access denied to this appointment")
        }

        return appointment
    }

    /**
     * Cancels an appointment
     */
    fun cancelAppointment(appointmentId: UUID, clientId: UUID, reason: String?): AppointmentDTO {
        val command = CancelAppointmentCommand(
            appointmentId = appointmentId,
            clientId = clientId,
            reason = reason
        )

        return appointmentService.cancelAppointment(command)
    }

    /**
     * Staff confirms an appointment
     */
    fun confirmAppointment(appointmentId: UUID): AppointmentDTO {
        val command = ConfirmAppointmentCommand(appointmentId)
        return appointmentService.confirmAppointment(command)
    }

    /**
     * Staff marks an appointment as completed
     */
    fun completeAppointment(appointmentId: UUID): AppointmentDTO {
        val command = CompleteAppointmentCommand(appointmentId)
        return appointmentService.completeAppointment(command)
    }

    /**
     * Staff marks a client as no-show
     */
    fun markClientAsNoShow(appointmentId: UUID): AppointmentDTO {
        val command = NoShowAppointmentCommand(appointmentId)
        return appointmentService.markAsNoShow(command)
    }

    /**
     * Gets all pending appointments for a business
     */
    fun getPendingAppointmentsForBusiness(businessId: UUID): List<AppointmentDTO> {
        return appointmentService.getPendingAppointments(businessId)
    }

    /**
     * Gets all appointments for a staff member on a given date
     */
    fun getStaffAppointmentsForDate(staffId: UUID, date: LocalDate): List<AppointmentDTO> {
        return appointmentService.getStaffAppointmentsForDate(staffId, date)
    }

    /**
     * Creates a client-friendly view of appointments
     */
    fun createClientAppointmentsView(clientId: UUID): ClientAppointmentsResponse {
        val allAppointments = appointmentService.getClientAppointments(clientId)
        val today = LocalDate.now()

        val upcoming = allAppointments
            .filter { it.date.isAfter(today.minusDays(1)) &&
                    (it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.CONFIRMED) }
            .sortedBy { it.date.atTime(it.startTime) }
            .map { createClientView(it, true) }

        val past = allAppointments
            .filter { it.date.isBefore(today) ||
                    (it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW) }
            .sortedByDescending { it.date.atTime(it.startTime) }
            .map { createClientView(it, false) }

        return ClientAppointmentsResponse(
            upcoming = upcoming,
            past = past
        )
    }

    /**
     * Creates a client-friendly view of a single appointment
     */
    private fun createClientView(appointment: AppointmentDTO, canCancel: Boolean): ClientAppointmentView {
        val canCancelFinal = canCancel &&
                (appointment.status == AppointmentStatus.SCHEDULED || appointment.status == AppointmentStatus.CONFIRMED)

        return ClientAppointmentView(
            id = appointment.id,
            businessName = appointment.businessName ?: "Unknown Business",
            staffName = appointment.staffName ?: "Unknown Staff",
            serviceName = appointment.serviceName ?: "Unknown Service",
            date = appointment.date,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            status = appointment.status.name,
            canCancel = canCancelFinal
        )
    }
}

/**
 * Request to book an appointment
 */
data class BookAppointmentRequest(
    val businessId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val notes: String? = null
)

/**
 * Request to cancel an appointment
 */
data class CancelAppointmentRequest(
    val reason: String? = null
)