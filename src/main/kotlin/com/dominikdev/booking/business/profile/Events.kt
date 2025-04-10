package com.dominikdev.booking.business.profile

import com.dominikdev.booking.shared.event.DomainEvent
import java.util.UUID

class BusinessProfileCreatedEvent(
    val businessId: UUID,
    val businessName: String
) : DomainEvent() {
    override val eventName: String = "business.profile.created"
}

class BusinessProfileUpdatedEvent(
    val businessId: UUID,
    val businessName: String
) : DomainEvent() {
    override val eventName: String = "business.profile.updated"
}