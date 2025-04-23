package com.dominikdev.booking.appointment.domain.model

import java.time.LocalTime

data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    init {
        require(!startTime.isAfter(endTime)) { "Start time must be before or equal to end time" }
    }

    fun overlaps(other: TimeSlot): Boolean {
        // If one slot ends before or at the same time the other starts, they don't overlap
        return !(endTime.isBefore(other.startTime) || endTime.equals(other.startTime) ||
                startTime.isAfter(other.endTime) || startTime.equals(other.endTime))
    }

    fun contains(timeSlot: TimeSlot): Boolean {
        return !startTime.isAfter(timeSlot.startTime) && !endTime.isBefore(timeSlot.endTime)
    }

    fun contains(time: LocalTime): Boolean {
        return !time.isBefore(startTime) && !time.isAfter(endTime)
    }

    fun durationMinutes(): Long {
        return java.time.Duration.between(startTime, endTime).toMinutes()
    }

    /**
     * Split a time slot into two parts, before and after the given appointment slot
     * Returns a list of new time slots.
     */
    fun splitByAppointment(appointmentSlot: TimeSlot): List<TimeSlot> {
        require(this.contains(appointmentSlot)) { "Appointment slot must be fully contained within this time slot" }

        val result = mutableListOf<TimeSlot>()

        // Add slot before appointment if it has duration
        if (startTime.isBefore(appointmentSlot.startTime)) {
            result.add(TimeSlot(startTime, appointmentSlot.startTime))
        }

        // Add slot after appointment if it has duration
        if (appointmentSlot.endTime.isBefore(endTime)) {
            result.add(TimeSlot(appointmentSlot.endTime, endTime))
        }

        return result
    }
}