package com.dominikdev.booking.appointment.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Represents a special schedule for a specific date (overrides weekly schedule)
 */
class SpecialDaySchedule private constructor(
    val id: UUID,
    val staffId: UUID,
    val date: LocalDate,
    private val timeSlots: MutableList<TimeSlot> = mutableListOf(),
    val appointmentOnly: Boolean = false
) {
    companion object {
        fun create(staffId: UUID, date: LocalDate, appointmentOnly: Boolean = false): SpecialDaySchedule {
            return SpecialDaySchedule(
                id = UUID.randomUUID(),
                staffId = staffId,
                date = date,
                appointmentOnly = appointmentOnly
            )
        }
    }

    fun addTimeSlot(startTime: LocalTime, endTime: LocalTime) {
        val newSlot = TimeSlot(startTime, endTime)

        // Check for overlaps before adding
        if (timeSlots.any { it.overlaps(newSlot) }) {
            throw AppointmentDomainException("New time slot overlaps with existing slots")
        }

        timeSlots.add(newSlot)
    }

    fun getTimeSlots(): List<TimeSlot> = timeSlots.toList()

    fun removeTimeSlot(startTime: LocalTime, endTime: LocalTime) {
        timeSlots.removeIf {
            it.startTime == startTime && it.endTime == endTime
        }
    }

    fun isAvailable(timeSlot: TimeSlot): Boolean {
        if (timeSlots.isEmpty()) return false

        return timeSlots.any { slot ->
            slot.startTime <= timeSlot.startTime && slot.endTime >= timeSlot.endTime
        }
    }
}
