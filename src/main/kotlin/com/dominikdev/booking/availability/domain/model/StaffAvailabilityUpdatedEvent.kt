package com.dominikdev.booking.availability.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDate
import java.util.UUID

/**
 * Event emitted when staff availability is updated for a day
 */
class StaffAvailabilityUpdatedEvent(
    val staffId: UUID,
    val businessId: UUID,
    val date: LocalDate,
    val previousTimeSlots: List<TimeSlot>,
    val currentTimeSlots: List<TimeSlot>
) : DomainEvent() {
    override val eventName: String = "staff.availability.updated"
    // Calculate what actually changed
    val added: List<TimeSlot> = currentTimeSlots.filterNot { current ->
        previousTimeSlots.any { prev ->
            prev.startTime == current.startTime && prev.endTime == current.endTime
        }
    }

    val removed: List<TimeSlot> = previousTimeSlots.filterNot { prev ->
        currentTimeSlots.any { current ->
            current.startTime == prev.startTime && current.endTime == prev.endTime
        }
    }
    val hasChanges: Boolean = added.isNotEmpty() || removed.isNotEmpty()
}