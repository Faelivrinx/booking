package com.dominikdev.booking.appointment.domain.repository

import com.dominikdev.booking.appointment.domain.model.StaffDailyAvailability
import java.time.LocalDate
import java.util.*

interface StaffAvailabilityRepository {
    /**
     * Save or update staff availability for a day
     */
    fun save(availability: StaffDailyAvailability): StaffDailyAvailability

    /**
     * Find staff availability by ID
     */
    fun findById(id: UUID): StaffDailyAvailability?

    /**
     * Find staff availability for a specific date
     */
    fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): StaffDailyAvailability?

    /**
     * Find staff availability for a date range
     */
    fun findByStaffIdAndDateRange(staffId: UUID, startDate: LocalDate, endDate: LocalDate): List<StaffDailyAvailability>

    /**
     * Find availability for all staff on a specific date for a business
     */
    fun findByBusinessIdAndDate(businessId: UUID, date: LocalDate): List<StaffDailyAvailability>

    /**
     * Delete staff availability for a specific date
     */
    fun deleteByStaffIdAndDate(staffId: UUID, date: LocalDate)

    /**
     * Check if staff has availability set for a date
     */
    fun existsByStaffIdAndDate(staffId: UUID, date: LocalDate): Boolean
}