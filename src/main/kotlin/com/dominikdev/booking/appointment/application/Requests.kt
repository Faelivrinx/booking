package com.dominikdev.booking.appointment.application

import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Command for booking a new appointment
 */
data class BookAppointmentCommand(
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val notes: String? = null
)

/**
 * Command for cancelling an appointment
 */
data class CancelAppointmentCommand(
    val appointmentId: UUID,
    val clientId: UUID,
    val reason: String? = null
)

/**
 * Command for confirming an appointment
 */
data class ConfirmAppointmentCommand(
    val appointmentId: UUID
)

/**
 * Command for marking an appointment as completed
 */
data class CompleteAppointmentCommand(
    val appointmentId: UUID
)

/**
 * Command for marking an appointment as no-show
 */
data class NoShowAppointmentCommand(
    val appointmentId: UUID
)

/**
 * Command for staff appointment cancellation
 */
data class StaffCancelAppointmentCommand(
    val appointmentId: UUID,
    val staffId: UUID,
    val reason: String
)

/**
 * Data Transfer Object for Appointment
 */
data class AppointmentDTO(
    val id: UUID,
    val businessId: UUID,
    val businessName: String?,
    val clientId: UUID,
    val clientName: String?,
    val staffId: UUID,
    val staffName: String?,
    val serviceId: UUID,
    val serviceName: String?,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Long,
    val status: AppointmentStatus,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Simplified appointment view for clients
 */
data class ClientAppointmentView(
    val id: UUID,
    val businessName: String,
    val staffName: String,
    val serviceName: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: String,
    val canCancel: Boolean
)

/**
 * Response for appointment searches
 */
data class AppointmentListResponse(
    val appointments: List<AppointmentDTO>,
    val total: Int
)

/**
 * Response for client's appointments
 */
data class ClientAppointmentsResponse(
    val upcoming: List<ClientAppointmentView>,
    val past: List<ClientAppointmentView>
)