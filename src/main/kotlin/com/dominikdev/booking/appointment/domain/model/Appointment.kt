package com.dominikdev.booking.appointment.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Appointment aggregate root - represents a booking for a service with a staff member
 */
class Appointment private constructor(
    val id: UUID,
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: AppointmentStatus,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    private val events: MutableList<AppointmentEvent> = mutableListOf()
) {
    companion object {
        /**
         * Creates a new appointment
         */
        fun schedule(
            businessId: UUID,
            clientId: UUID,
            staffId: UUID,
            serviceId: UUID,
            date: LocalDate,
            startTime: LocalTime,
            endTime: LocalTime,
            notes: String? = null
        ): Appointment {
            val now = LocalDateTime.now()
            val appointmentId = UUID.randomUUID()

            // Validate time range
            if (startTime >= endTime) {
                throw AppointmentException("Start time must be before end time")
            }

            // Create the appointment
            val appointment = Appointment(
                id = appointmentId,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = AppointmentStatus.SCHEDULED,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )

            // Register the appointment scheduled event
            appointment.registerEvent(
                AppointmentScheduledEvent(
                    appointmentId = appointmentId,
                    businessId = businessId,
                    clientId = clientId,
                    staffId = staffId,
                    serviceId = serviceId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime
                )
            )

            return appointment
        }

        /**
         * Reconstitutes an appointment from storage (no events generated)
         */
        fun reconstitute(
            id: UUID,
            businessId: UUID,
            clientId: UUID,
            staffId: UUID,
            serviceId: UUID,
            date: LocalDate,
            startTime: LocalTime,
            endTime: LocalTime,
            status: AppointmentStatus,
            notes: String?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): Appointment {
            return Appointment(
                id = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = status,
                notes = notes,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    /**
     * Confirms a scheduled appointment
     */
    fun confirm(): Appointment {
        if (status != AppointmentStatus.SCHEDULED) {
            throw AppointmentException("Only scheduled appointments can be confirmed")
        }

        val updatedAppointment = this.copy(
            status = AppointmentStatus.CONFIRMED,
            updatedAt = LocalDateTime.now()
        )

        updatedAppointment.registerEvent(
            AppointmentConfirmedEvent(
                appointmentId = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                date = date,
                startTime = startTime,
                endTime = endTime
            )
        )

        return updatedAppointment
    }

    /**
     * Cancels an appointment
     */
    fun cancel(reason: String? = null): Appointment {
        if (status == AppointmentStatus.CANCELLED) {
            return this
        }

        if (status == AppointmentStatus.COMPLETED) {
            throw AppointmentException("Completed appointments cannot be cancelled")
        }

        val updatedAppointment = this.copy(
            status = AppointmentStatus.CANCELLED,
            notes = reason ?: notes,
            updatedAt = LocalDateTime.now()
        )

        updatedAppointment.registerEvent(
            AppointmentCancelledEvent(
                appointmentId = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                cancellationReason = reason
            )
        )

        return updatedAppointment
    }

    /**
     * Marks an appointment as completed
     */
    fun complete(): Appointment {
        if (status != AppointmentStatus.CONFIRMED) {
            throw AppointmentException("Only confirmed appointments can be completed")
        }

        val updatedAppointment = this.copy(
            status = AppointmentStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )

        updatedAppointment.registerEvent(
            AppointmentCompletedEvent(
                appointmentId = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                date = date,
                startTime = startTime,
                endTime = endTime
            )
        )

        return updatedAppointment
    }

    /**
     * Marks a client as no-show for an appointment
     */
    fun markAsNoShow(): Appointment {
        if (status != AppointmentStatus.CONFIRMED) {
            throw AppointmentException("Only confirmed appointments can be marked as no-show")
        }

        val updatedAppointment = this.copy(
            status = AppointmentStatus.NO_SHOW,
            updatedAt = LocalDateTime.now()
        )

        updatedAppointment.registerEvent(
            AppointmentNoShowEvent(
                appointmentId = id,
                businessId = businessId,
                clientId = clientId,
                staffId = staffId,
                serviceId = serviceId,
                date = date,
                startTime = startTime,
                endTime = endTime
            )
        )

        return updatedAppointment
    }

    /**
     * Gets the duration of the appointment in minutes
     */
    fun durationMinutes(): Long {
        return Duration.between(startTime, endTime).toMinutes()
    }

    /**
     * Gets all events generated by this aggregate
     */
    fun getEvents(): List<AppointmentEvent> = events.toList()

    /**
     * Clears all events (typically after they've been published)
     */
    fun clearEvents() {
        events.clear()
    }

    /**
     * Registers a new domain event
     */
    private fun registerEvent(event: AppointmentEvent) {
        events.add(event)
    }

    /**
     * Creates a copy of this appointment with updated fields
     */
    private fun copy(
        status: AppointmentStatus = this.status,
        notes: String? = this.notes,
        updatedAt: LocalDateTime = this.updatedAt
    ): Appointment {
        return Appointment(
            id = this.id,
            businessId = this.businessId,
            clientId = this.clientId,
            staffId = this.staffId,
            serviceId = this.serviceId,
            date = this.date,
            startTime = this.startTime,
            endTime = this.endTime,
            status = status,
            notes = notes,
            createdAt = this.createdAt,
            updatedAt = updatedAt,
            events = this.events
        )
    }
}