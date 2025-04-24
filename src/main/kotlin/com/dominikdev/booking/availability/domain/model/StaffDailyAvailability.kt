package com.dominikdev.booking.availability.domain.model
import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class StaffDailyAvailability(
    val id: UUID = UUID.randomUUID(),
    val staffId: UUID,
    val businessId: UUID,
    val date: LocalDate,
    private val timeSlots: MutableList<TimeSlot> = mutableListOf(),
) {
    private val events = mutableListOf<DomainEvent>()

    fun setAvailability(timeSlots: List<TimeSlot>) {
        this.timeSlots.clear()
        timeSlots.forEach { addTimeSlot(it.startTime, it.endTime) }

        events.add(StaffDailyAvailabilityUpdatedEvent(
            staffId = staffId,
            businessId = businessId,
            date = date
        ))
    }

    fun addTimeSlot(startTime: LocalTime, endTime: LocalTime) {
        val timeSlot = TimeSlot(startTime, endTime)

        // Check for overlaps
        if (hasOverlappingSlot(timeSlot)) {
            throw OverlappingTimeSlots("New time slot overlaps with existing slots")
        }

        timeSlots.add(timeSlot)

        events.add(StaffDailyAvailabilityUpdatedEvent(
            staffId = staffId,
            businessId = businessId,
            date = date
        ))
    }

    fun removeTimeSlot(timeSlot: TimeSlot): Boolean {
        val removed = timeSlots.removeIf {
            it.startTime == timeSlot.startTime && it.endTime == timeSlot.endTime
        }

        if (removed) {
            events.add(StaffDailyAvailabilityUpdatedEvent(
                staffId = staffId,
                businessId = businessId,
                date = date
            ))
        }

        return removed
    }

    fun getTimeSlots(): List<TimeSlot> = timeSlots.toList()

    fun hasOverlappingSlot(timeSlot: TimeSlot): Boolean {
        return timeSlots.any { it.overlaps(timeSlot) }
    }

    fun isAvailable(timeSlot: TimeSlot): Boolean {
        if (timeSlots.isEmpty()) return false
        return timeSlots.any { it.contains(timeSlot) }
    }

    fun isAvailable(startTime: LocalTime, endTime: LocalTime): Boolean {
        return isAvailable(TimeSlot(startTime, endTime))
    }

    fun isEmpty(): Boolean = timeSlots.isEmpty()

    fun getEvents(): List<DomainEvent> = events.toList()

    fun clearEvents() {
        events.clear()
    }
}