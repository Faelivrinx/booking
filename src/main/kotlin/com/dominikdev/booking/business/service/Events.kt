package com.dominikdev.booking.business.service

import com.dominikdev.booking.shared.event.DomainEvent
import java.util.UUID

/**
 * Event triggered when a new service is created
 */
class ServiceCreatedEvent(
    val serviceId: UUID,
    val businessId: UUID,
    val serviceName: String
) : DomainEvent() {
    override val eventName: String = "service.created"
}

/**
 * Event triggered when a service is updated
 */
class ServiceUpdatedEvent(
    val serviceId: UUID,
    val businessId: UUID,
    val serviceName: String
) : DomainEvent() {
    override val eventName: String = "service.updated"
}

/**
 * Event triggered when a service is deleted
 */
class ServiceDeletedEvent(
    val serviceId: UUID,
    val businessId: UUID,
    val serviceName: String
) : DomainEvent() {
    override val eventName: String = "service.deleted"
}