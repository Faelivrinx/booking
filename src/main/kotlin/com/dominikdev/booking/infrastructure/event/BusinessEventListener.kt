package com.dominikdev.booking.infrastructure.event

import com.dominikdev.booking.domain.business.BusinessCreatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BusinessEventListener {

    private val logger = KotlinLogging.logger {}

    @EventListener
    fun handleBusinessCreatedEvent(event: BusinessCreatedEvent) {
        logger.info { "Business created: ${event.businessId} (${event.name}, ${event.email})" }

    }
}