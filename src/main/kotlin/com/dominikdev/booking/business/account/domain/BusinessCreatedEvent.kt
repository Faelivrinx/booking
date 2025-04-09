package com.dominikdev.booking.business.account.domain

import com.dominikdev.booking.shared.event.DomainEvent

class BusinessCreatedEvent(
    val businessId: BusinessId,
    val keycloakId: String,
    val name: String,
    val email: String
) : DomainEvent() {
    override val eventName: String = "business.created"
}