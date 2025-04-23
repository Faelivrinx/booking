package com.dominikdev.booking.appointment.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.*
import java.util.UUID

/**
 * Aggregate root for appointments
 */

class Appointment(
    val id: UUID,
    val businessId: UUID,
    val serviceId: UUID,
    val staffId: UUID,
    val clientId: UUID,
    val date: LocalDate,
    private val startTime: LocalTime,
    private val endTime: LocalTime,
    val timeZone: ZoneId,
    private var status: AppointmentStatus,
    private val events: MutableList<DomainEvent> = mutableListOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    private var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            businessId: UUID,
            serviceId: UUID,
            staffId: UUID,
            clientId: UUID,
            date: LocalDate,
            startTime: LocalTime,
            endTime: LocalTime,
            timeZone: ZoneId = ZoneId.systemDefault()
        ): Appointment {
            val id = UUID.randomUUID()
            val now = LocalDateTime.now()

            val appointment = Appointment(
                id = id,
                businessId = businessId,
                serviceId = serviceId,
                staffId = staffId,
                clientId = clientId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                timeZone = timeZone,
                status = AppointmentStatus.SCHEDULED,
                createdAt = now,
                updatedAt = now
            )

            // Create and add domain event
            appointment.addEvent(
                AppointmentCreatedEvent(
                    appointmentId = id,
                    businessId = businessId,
                    serviceId = serviceId,
                    staffId = staffId,
                    clientId = clientId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    timeZone = timeZone.id
                )
            )

            return appointment
        }
    }

    fun getTimeSlot(): TimeSlot = TimeSlot(startTime, endTime)

    fun getStartTime(): LocalTime = startTime

    fun getEndTime(): LocalTime = endTime

    fun getStatus(): AppointmentStatus = status

    fun getEvents(): List<DomainEvent> = events.toList()

    fun clearEvents() {
        events.clear()
    }

    private fun addEvent(event: DomainEvent) {
        events.add(event)
    }

    fun confirm(): Boolean {
        if (status != AppointmentStatus.SCHEDULED) {
            return false
        }

        status = AppointmentStatus.CONFIRMED
        updatedAt = LocalDateTime.now()

        addEvent(
            AppointmentConfirmedEvent(
                appointmentId = id,
                businessId = businessId,
                staffId = staffId,
                clientId = clientId,
                confirmedAt = updatedAt
            )
        )

        return true
    }

    fun cancel(reason: String? = null): Boolean {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW) {
            return false
        }

        status = AppointmentStatus.CANCELLED
        updatedAt = LocalDateTime.now()

        addEvent(
            AppointmentCancelledEvent(
                appointmentId = id,
                businessId = businessId,
                staffId = staffId,
                clientId = clientId,
                cancelledAt = updatedAt,
                reason = reason
            )
        )

        return true
    }

    fun markAsCompleted(): Boolean {
        if (status != AppointmentStatus.CONFIRMED && status != AppointmentStatus.SCHEDULED) {
            return false
        }

        status = AppointmentStatus.COMPLETED
        updatedAt = LocalDateTime.now()

        addEvent(
            AppointmentCompletedEvent(
                appointmentId = id,
                clientId = clientId,
                staffId = staffId,
                completedAt = updatedAt
            )
        )

        return true
    }

    fun markAsNoShow(): Boolean {
        if (status != AppointmentStatus.CONFIRMED && status != AppointmentStatus.SCHEDULED) {
            return false
        }

        status = AppointmentStatus.NO_SHOW
        updatedAt = LocalDateTime.now()

        addEvent(
            AppointmentNoShowEvent(
                appointmentId = id,
                clientId = clientId,
                staffId = staffId,
                missedAt = updatedAt
            )
        )

        return true
    }

    fun overlaps(other: Appointment): Boolean {
        if (staffId != other.staffId || date != other.date) {
            return false
        }

        val thisSlot = TimeSlot(startTime, endTime)
        val otherSlot = TimeSlot(other.startTime, other.endTime)

        return thisSlot.overlaps(otherSlot)
    }

    fun getStartDateTime(): LocalDateTime {
        return LocalDateTime.of(date, startTime)
    }

    fun getEndDateTime(): LocalDateTime {
        return LocalDateTime.of(date, endTime)
    }

    fun getStartDateTimeWithZone(): ZonedDateTime {
        return ZonedDateTime.of(getStartDateTime(), timeZone)
    }

    fun getEndDateTimeWithZone(): ZonedDateTime {
        return ZonedDateTime.of(getEndDateTime(), timeZone)
    }
}