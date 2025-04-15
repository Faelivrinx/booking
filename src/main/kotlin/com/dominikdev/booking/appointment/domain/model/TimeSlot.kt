package com.dominikdev.booking.appointment.domain.model

import java.time.LocalTime

data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    init {
        require(startTime.isBefore(endTime)) { "Start time must be before end time" }
    }

    fun overlaps(other: TimeSlot): Boolean {
        return !(endTime.isBefore(other.startTime) || startTime.isAfter(other.endTime))
    }

    fun contains(time: LocalTime): Boolean {
        return !time.isBefore(startTime) && !time.isAfter(endTime)
    }

    fun duration(): Long {
        return java.time.Duration.between(startTime, endTime).toMinutes()
    }
}