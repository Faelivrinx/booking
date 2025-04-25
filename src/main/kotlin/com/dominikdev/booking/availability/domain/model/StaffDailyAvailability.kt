package com.dominikdev.booking.availability.domain.model
import com.dominikdev.booking.shared.event.DomainEvent
import io.github.oshai.kotlinlogging.KotlinLogging
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

    companion object {
        private val logger = KotlinLogging.logger {  }

        /**
         * Factory method to reconstitute an entity from persistence
         * without triggering domain logic or generating events
         */
        fun reconstitute(
            id: UUID,
            staffId: UUID,
            businessId: UUID,
            date: LocalDate,
            timeSlots: List<TimeSlot>
        ): StaffDailyAvailability {
            val instance = StaffDailyAvailability(
                id = id,
                staffId = staffId,
                businessId = businessId,
                date = date
            )
            // Directly set the time slots without validation or event generation
            instance.timeSlots.addAll(timeSlots)
            return instance
        }
    }

    private val events = mutableListOf<DomainEvent>()

    // Modified to create a single event with clear information about what changed
    fun setAvailability(newTimeSlots: List<TimeSlot>): AvailabilityChangeResult {
        val oldTimeSlots = timeSlots.toList() // Capture previous state

        timeSlots.clear()
        newTimeSlots.forEach { slot ->
            if (validateTimeSlot(slot)) {
                timeSlots.add(slot)
            } else {
                throw OverlappingTimeSlots("Invalid time slot: $slot")
            }
        }

        // Single event with previous and new state
        val event = StaffAvailabilityUpdatedEvent(
            staffId = staffId,
            businessId = businessId,
            date = date,
            previousTimeSlots = oldTimeSlots,
            currentTimeSlots = timeSlots.toList()
        )

        events.add(event)

        return AvailabilityChangeResult(
            added = newTimeSlots.filter { !oldTimeSlots.contains(it) },
            removed = oldTimeSlots.filter { !newTimeSlots.contains(it) }
        )
    }


    fun getTimeSlots(): List<TimeSlot> = timeSlots.toList()

    private fun validateTimeSlot(timeSlot: TimeSlot): Boolean {
        return !timeSlots.any { it.overlaps(timeSlot) }
    }

    fun isEmpty(): Boolean = timeSlots.isEmpty()

    fun getEvents(): List<DomainEvent> = events.toList()

    fun clearEvents() {
        events.clear()
    }
}

data class AvailabilityChangeResult(
    val added: List<TimeSlot>,
    val removed: List<TimeSlot>
)