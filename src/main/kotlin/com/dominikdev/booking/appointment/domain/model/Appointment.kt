package com.dominikdev.booking.appointment.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Appointment aggregate root
 */
class Appointment private constructor(
    val id: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    private var startDateTime: LocalDateTime,
    private var endDateTime: LocalDateTime,
    private var status: AppointmentStatus,
    val notes: String?,
    val createdAt: LocalDateTime,
    private var updatedAt: LocalDateTime
) {
    private val events = mutableListOf<DomainEvent>()

    companion object {
        fun create(
            businessId: UUID,
            clientId: UUID,
            staffId: UUID,
            serviceId: UUID,
            startDateTime: LocalDateTime,
            endDateTime: LocalDateTime,
            notes: String? = null
        ): Appointment {
            val id = UUID.randomUUID()
            val now = LocalDateTime.now()

            val appointment = Appointment(
                id = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                status = AppointmentStatus.SCHEDULED,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )

            // Add creation event
            appointment.events.add(
                AppointmentCreatedEvent(
                    appointmentId = id,
                    businessId = businessId,
                    clientId = clientId,
                    staffId = staffId,
                    serviceId = serviceId,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime
                )
            )

            return appointment
        }
    }

    fun cancel(): Boolean {
        if (status != AppointmentStatus.SCHEDULED && status != AppointmentStatus.CONFIRMED) {
            return false
        }

        status = AppointmentStatus.CANCELLED
        updatedAt = LocalDateTime.now()

        events.add(
            AppointmentCancelledEvent(
                appointmentId = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                cancelledAt = updatedAt
            )
        )

        return true
    }

    fun markNoShow(): Boolean {
        if (status != AppointmentStatus.SCHEDULED && status != AppointmentStatus.CONFIRMED) {
            return false
        }

        status = AppointmentStatus.NO_SHOW
        updatedAt = LocalDateTime.now()

        events.add(
            AppointmentNoShowEvent(
                appointmentId = id,
                clientId = clientId,
                staffId = staffId,
                missedAt = updatedAt
            )
        )

        return true
    }

    fun markCompleted(): Boolean {
        if (status != AppointmentStatus.SCHEDULED && status != AppointmentStatus.CONFIRMED) {
            return false
        }

        status = AppointmentStatus.COMPLETED
        updatedAt = LocalDateTime.now()

        events.add(
            AppointmentCompletedEvent(
                appointmentId = id,
                clientId = clientId,
                staffId = staffId,
                completedAt = updatedAt
            )
        )

        return true
    }

    fun confirm(): Boolean {
        if (status != AppointmentStatus.SCHEDULED) {
            return false
        }

        status = AppointmentStatus.CONFIRMED
        updatedAt = LocalDateTime.now()
        return true
    }

    fun overlaps(other: Appointment): Boolean {
        if (staffId != other.staffId) {
            return false
        }

        return !(endDateTime.isBefore(other.startDateTime) || startDateTime.isAfter(other.endDateTime))
    }

    fun getDate(): LocalDate = startDateTime.toLocalDate()
    fun getStartTime(): LocalTime = startDateTime.toLocalTime()
    fun getEndTime(): LocalTime = endDateTime.toLocalTime()
    fun getStatus(): AppointmentStatus = status
    fun getStartDateTime(): LocalDateTime = startDateTime
    fun getEndDateTime(): LocalDateTime = endDateTime
    fun getEvents(): List<DomainEvent> = events.toList()
    fun clearEvents() { events.clear() }
}