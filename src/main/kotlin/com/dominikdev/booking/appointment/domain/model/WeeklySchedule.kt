package com.dominikdev.booking.appointment.domain.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

/**
 * Represents weekly recurring availability for a staff member
 */
class WeeklySchedule private constructor(
    val id: UUID,
    val staffId: UUID,
    private val scheduleItems: MutableMap<DayOfWeek, MutableList<TimeSlot>> = mutableMapOf()
) {
    companion object {
        fun create(staffId: UUID): WeeklySchedule {
            return WeeklySchedule(
                id = UUID.randomUUID(),
                staffId = staffId
            )
        }
    }

    fun addTimeSlot(dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime) {
        val timeSlot = TimeSlot(startTime, endTime)

        // Check for overlapping time slots
        if (scheduleItems[dayOfWeek]?.any { it.overlaps(timeSlot) } == true) {
            throw AppointmentDomainException("New time slot overlaps with existing slots")
        }

        if (scheduleItems.containsKey(dayOfWeek)) {
            scheduleItems[dayOfWeek]?.add(timeSlot)
        } else {
            scheduleItems[dayOfWeek] = mutableListOf(timeSlot)
        }
    }

    fun getTimeSlots(dayOfWeek: DayOfWeek): List<TimeSlot> {
        return scheduleItems[dayOfWeek]?.toList() ?: emptyList()
    }

    fun removeTimeSlot(dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime) {
        scheduleItems[dayOfWeek]?.removeIf {
            it.startTime == startTime && it.endTime == endTime
        }
    }

    fun isAvailable(dayOfWeek: DayOfWeek, timeSlot: TimeSlot): Boolean {
        val slots = scheduleItems[dayOfWeek] ?: return false

        return slots.any { slot ->
            slot.startTime <= timeSlot.startTime && slot.endTime >= timeSlot.endTime
        }
    }
}