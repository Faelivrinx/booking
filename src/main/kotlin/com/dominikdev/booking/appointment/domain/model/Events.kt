package com.dominikdev.booking.appointment.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Event emitted when a new appointment is created
 */
class AppointmentCreatedEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val serviceId: UUID,
    val staffId: UUID,
    val clientId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val timeZone: String
) : DomainEvent() {
    override val eventName: String = "appointment.created"
}

/**
 * Event emitted when an appointment is confirmed
 */
class AppointmentConfirmedEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val staffId: UUID,
    val clientId: UUID,
    val confirmedAt: LocalDateTime
) : DomainEvent() {
    override val eventName: String = "appointment.confirmed"
}

/**
 * Event emitted when an appointment is cancelled
 */
class AppointmentCancelledEvent(
    val appointmentId: UUID,
    val businessId: UUID,
    val staffId: UUID,
    val clientId: UUID,
    val cancelledAt: LocalDateTime,
    val reason: String? = null
) : DomainEvent() {
    override val eventName: String = "appointment.cancelled"
}

/**
 * Event emitted when an appointment is completed
 */
class AppointmentCompletedEvent(
    val appointmentId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val completedAt: LocalDateTime
) : DomainEvent() {
    override val eventName: String = "appointment.completed"
}

/**
 * Event emitted when a client doesn't show up for an appointment
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
 * Event emitted when staff availability is updated for a day
 */
class StaffDailyAvailabilityUpdatedEvent(
    val staffId: UUID,
    val businessId: UUID,
    val date: LocalDate
) : DomainEvent() {
    override val eventName: String = "staff.availability.updated"
}

/**
 * Event emitted when a staff-service association is updated
 */
class StaffServiceAssociationUpdatedEvent(
    val staffId: UUID,
    val serviceId: UUID,
    val businessId: UUID,
    val active: Boolean
) : DomainEvent() {
    override val eventName: String = "staff.service.association.updated"
}