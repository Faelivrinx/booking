package com.dominikdev.booking.appointment.infrastructure.readmodel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

/**
 * Repository for staff daily schedules
 */
@Repository
interface StaffDailyScheduleRepository : JpaRepository<StaffDailySchedule, UUID> {

    /**
     * Find schedule for a staff member on a specific date
     */
    fun findByStaffIdAndDateOrderByStartTimeAsc(
        staffId: UUID,
        date: LocalDate
    ): List<StaffDailySchedule>

    /**
     * Find schedule for a staff member in a date range
     */
    fun findByStaffIdAndDateBetweenOrderByDateAscStartTimeAsc(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<StaffDailySchedule>

    /**
     * Find all staff schedules for a business on a date
     */
    fun findByBusinessIdAndDateOrderByStaffIdAscStartTimeAsc(
        businessId: UUID,
        date: LocalDate
    ): List<StaffDailySchedule>

    /**
     * Delete schedule for a staff member on a specific date
     */
    fun deleteByStaffIdAndDate(staffId: UUID, date: LocalDate)
}