package com.dominikdev.booking.availability.domain.repository
import com.dominikdev.booking.availability.domain.model.StaffDailyAvailability
import java.time.LocalDate
import java.util.UUID

interface StaffAvailabilityRepository {
    fun save(availability: StaffDailyAvailability): StaffDailyAvailability
    fun findById(id: UUID): StaffDailyAvailability?
    fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): StaffDailyAvailability?
    fun findByStaffIdAndDateRange(staffId: UUID, startDate: LocalDate, endDate: LocalDate): List<StaffDailyAvailability>
    fun findByBusinessIdAndDate(businessId: UUID, date: LocalDate): List<StaffDailyAvailability>
    fun deleteByStaffIdAndDate(staffId: UUID, date: LocalDate)
    fun existsByStaffIdAndDate(staffId: UUID, date: LocalDate): Boolean
}