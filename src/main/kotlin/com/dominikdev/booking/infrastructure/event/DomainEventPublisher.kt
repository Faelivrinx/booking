package com.dominikdev.booking.infrastructure.event

import com.dominikdev.booking.domain.event.DomainEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DomainEventPublisher(private val applicationEventPublisher: ApplicationEventPublisher) {

    private val logger = KotlinLogging.logger {}

    fun publish(event: DomainEvent) {
        logger.debug { "Publishing domain event: ${event.eventName} (${event.eventId})" }
        applicationEventPublisher.publishEvent(event)
    }

    fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { publish(it) }
    }
}