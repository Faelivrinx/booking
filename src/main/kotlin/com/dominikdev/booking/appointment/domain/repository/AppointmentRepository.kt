package com.dominikdev.booking.appointment.domain.repository

import com.dominikdev.booking.appointment.domain.model.Appointment
import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Repository interface for appointment operations
 */
interface AppointmentRepository {
    /**
     * Save or update an appointment
     */
    fun save(appointment: Appointment): Appointment

    /**
     * Find an appointment by ID
     */
    fun findById(id: UUID): Appointment?

    /**
     * Find appointments for a business in a date range
     */
    fun findByBusinessIdAndDateRange(
        businessId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment>

    /**
     * Find appointments for a staff member in a date range
     */
    fun findByStaffIdAndDateRange(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment>

    /**
     * Find appointments for a client in a date range
     */
    fun findByClientIdAndDateRange(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment>

    /**
     * Find appointments for a staff member with specific statuses in a date range
     */
    fun findByStaffIdAndStatusAndDateRange(
        staffId: UUID,
        statuses: List<AppointmentStatus>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment>

    /**
     * Find appointments that overlap with a given time slot
     */
    fun findOverlappingAppointments(
        staffId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        excludeAppointmentId: UUID? = null
    ): List<Appointment>

    /**
     * Check if there's an existing appointment that overlaps with a given time slot
     */
    fun existsOverlappingAppointment(
        staffId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        excludeAppointmentId: UUID? = null
    ): Boolean

    /**
     * Delete an appointment by ID
     */
    fun deleteById(id: UUID)
}