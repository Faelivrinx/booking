package com.dominikdev.booking.domain.business

import com.dominikdev.booking.domain.event.DomainEvent

class BusinessCreatedEvent(
    val businessId: BusinessId,
    val keycloakId: String,
    val name: String,
    val email: String
) : DomainEvent() {
    override val eventName: String = "business.created"
}