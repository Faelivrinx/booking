package com.dominikdev.booking.appointment.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Aggregate root for staff availability
 */
class StaffAvailability private constructor(
    val id: UUID,
    val staffId: UUID,
    val businessId: UUID,
    val weeklySchedule: WeeklySchedule,
    private val specialDaySchedules: MutableMap<LocalDate, SpecialDaySchedule> = mutableMapOf()
) {
    companion object {
        fun create(staffId: UUID, businessId: UUID): StaffAvailability {
            return StaffAvailability(
                id = UUID.randomUUID(),
                staffId = staffId,
                businessId = businessId,
                weeklySchedule = WeeklySchedule.create(staffId)
            )
        }
    }

    fun addSpecialDaySchedule(specialDay: SpecialDaySchedule) {
        specialDaySchedules[specialDay.date] = specialDay
    }

    fun getSpecialDaySchedule(date: LocalDate): SpecialDaySchedule? {
        return specialDaySchedules[date]
    }

    fun isAvailable(date: LocalDate, timeSlot: TimeSlot): Boolean {
        // First check for special schedule for this day
        val specialSchedule = specialDaySchedules[date]
        if (specialSchedule != null) {
            return specialSchedule.isAvailable(timeSlot)
        }

        // Otherwise, check against weekly schedule
        val dayOfWeek = date.dayOfWeek
        return weeklySchedule.isAvailable(dayOfWeek, timeSlot)
    }

    fun isAvailable(date: LocalDate, startTime: LocalTime, endTime: LocalTime): Boolean {
        return isAvailable(date, TimeSlot(startTime, endTime))
    }
}
