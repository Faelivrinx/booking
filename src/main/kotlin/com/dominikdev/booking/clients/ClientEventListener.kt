package com.dominikdev.booking.clients

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ClientEventListener {

    private val logger = KotlinLogging.logger {}

    @EventListener
    fun handleClientRegisteredEvent(event: ClientRegisteredEvent) {
        logger.info { "Client registered: ${event.clientId} (${event.email}, ${event.phoneNumber})" }
    }

    @EventListener
    fun handleClientActivatedEvent(event: ClientActivatedEvent) {
        logger.info { "Client activated: ${event.clientId} (${event.email})" }
    }

    @EventListener
    fun handleClientVerificationCodeRegeneratedEvent(event: ClientVerificationCodeRegeneratedEvent) {
        logger.info { "Client verification code regenerated for: ${event.clientId} (${event.phoneNumber})" }
    }
}