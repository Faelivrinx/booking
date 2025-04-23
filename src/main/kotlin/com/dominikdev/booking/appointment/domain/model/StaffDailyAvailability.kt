package com.dominikdev.booking.appointment.domain.model

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
        this.timeSlots.forEach { removeTimeSlot(it) }
        timeSlots.forEach { addTimeSlot(it.startTime, it.endTime) }
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

    fun removeTimeSlot(timeSlot: TimeSlot) {
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

    /**
     * Apply an appointment by splitting the affected time slot
     * This ensures availability reflects booked appointments
     * TODO: consider the naming to more general like `splitByTimeSlot`
     */
    fun applyAppointment(appointmentSlot: TimeSlot): Boolean {
        // Find the affected time slot
        val affectedSlotIndex = timeSlots.indexOfFirst { it.contains(appointmentSlot) }

        if (affectedSlotIndex == -1) {
            return false // No matching slot found
        }

        val affectedSlot = timeSlots[affectedSlotIndex]

        // Remove the affected slot
        timeSlots.removeAt(affectedSlotIndex)

        // Add the split slots (before and after the appointment)
        val splitSlots = affectedSlot.splitByAppointment(appointmentSlot)
        timeSlots.addAll(splitSlots)

        events.add(StaffDailyAvailabilityUpdatedEvent(
            staffId = staffId,
            businessId = businessId,
            date = date
        ))

        return true
    }

    /**
     * Get and clear domain events
     */
    fun getEvents(): List<DomainEvent> = events.toList()

    fun clearEvents() {
        events.clear()
    }
}