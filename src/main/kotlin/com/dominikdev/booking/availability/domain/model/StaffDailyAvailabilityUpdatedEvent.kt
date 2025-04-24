package com.dominikdev.booking.availability.domain.model

import com.dominikdev.booking.shared.event.DomainEvent
import java.time.LocalDate
import java.util.UUID

/**
 * Event emitted when staff availability is updated for a day
 */
class StaffDailyAvailabilityUpdatedEvent(
    val staffId: UUID,
    val businessId: UUID,
    val date: LocalDate
) : DomainEvent() {
    override val eventName: String = "staff.availability.updated"
}