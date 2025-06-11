package com.dominikdev.booking.appointment.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Base class for all appointment-related events
 */
sealed class AppointmentEvent : DomainEvent()

/**
 * Event emitted when a new appointment is scheduled
 */
class AppointmentScheduledEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
) : AppointmentEvent() {
    override val eventName: String = "appointment.scheduled"
}

/**
 * Event emitted when an appointment is confirmed
 */
class AppointmentConfirmedEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
) : AppointmentEvent() {
    override val eventName: String = "appointment.confirmed"
}

/**
 * Event emitted when an appointment is cancelled
 */
class AppointmentCancelledEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val cancellationReason: String? = null
) : AppointmentEvent() {
    override val eventName: String = "appointment.cancelled"
}

/**
 * Event emitted when an appointment is completed
 */
class AppointmentCompletedEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
) : AppointmentEvent() {
    override val eventName: String = "appointment.completed"
}

/**
 * Event emitted when a client doesn't show up for an appointment
 */
class AppointmentNoShowEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
) : AppointmentEvent() {
    override val eventName: String = "appointment.no_show"
}