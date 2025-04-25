package com.dominikdev.booking.availability.domain.model

import java.time.LocalTime

data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    init {
        require(!startTime.isAfter(endTime)) { "Start time must be before end time" }
    }

    fun overlaps(other: TimeSlot): Boolean {
        return !(endTime.isBefore(other.startTime) || endTime.equals(other.startTime) ||
                startTime.isAfter(other.endTime) || startTime.equals(other.endTime))
    }

    fun contains(timeSlot: TimeSlot): Boolean {
        return !startTime.isAfter(timeSlot.startTime) && !endTime.isBefore(timeSlot.endTime)
    }

    fun durationMinutes(): Long {
        return java.time.Duration.between(startTime, endTime).toMinutes()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeSlot) return false

        return startTime == other.startTime && endTime == other.endTime
    }

    override fun hashCode(): Int {
        var result = startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }
}