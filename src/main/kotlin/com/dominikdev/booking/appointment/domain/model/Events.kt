package com.dominikdev.booking.appointment.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDateTime
import java.util.*

/**
 * Appointment created event
 */
class AppointmentCreatedEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime
) : DomainEvent() {
    override val eventName: String = "appointment.created"
}

/**
 * Appointment cancelled event
 */
class AppointmentCancelledEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val cancelledAt: LocalDateTime
) : DomainEvent() {
    override val eventName: String = "appointment.cancelled"
}

/**
 * Appointment no-show event
 */
class AppointmentNoShowEvent(
    val appointmentId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val missedAt: LocalDateTime
) : DomainEvent() {
    override val eventName: String = "appointment.no_show"
}

/**
 * Appointment completed event
 */
class AppointmentCompletedEvent(
    val appointmentId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val completedAt: LocalDateTime
) : DomainEvent() {
    override val eventName: String = "appointment.completed"
}