package com.dominikdev.booking.appointment.domain.repository

import com.dominikdev.booking.appointment.domain.model.Appointment
import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import java.time.LocalDate
import java.util.UUID

interface AppointmentRepository {
    /**
     * Saves an appointment
     */
    fun save(appointment: Appointment): Appointment

    /**
     * Finds an appointment by ID
     */
    fun findById(id: UUID): Appointment?

    /**
     * Finds all appointments for a client
     */
    fun findByClientId(clientId: UUID): List<Appointment>

    /**
     * Finds client's appointments with given statuses
     */
    fun findByClientIdAndStatuses(clientId: UUID, statuses: List<AppointmentStatus>): List<Appointment>

    /**
     * Finds client's appointments for a date range
     */
    fun findByClientIdAndDateRange(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment>

    /**
     * Finds all staff appointments for a date
     */
    fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): List<Appointment>

    /**
     * Find all pending appointments for a business
     * (scheduled or confirmed, not cancelled or completed)
     */
    fun findPendingAppointmentsByBusiness(
        businessId: UUID,
        fromDate: LocalDate
    ): List<Appointment>

    /**
     * Delete an appointment
     */
    fun delete(id: UUID)

    /**
     * Finds all appointments for a staff member
     */
    fun findByStaffId(staffId: UUID): List<Appointment>

    /**
     * Finds staff appointments for a date range
     */
    fun findByStaffIdAndDateRange(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment>

    /**
     * Finds staff appointments with given statuses after a specific date
     */
    fun findByStaffIdAndStatusesAndDateAfter(
        staffId: UUID,
        statuses: List<AppointmentStatus>,
        date: LocalDate
    ): List<Appointment>
}