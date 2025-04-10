package com.dominikdev.booking.clients.identity

import com.dominikdev.booking.shared.event.DomainEvent
import java.util.UUID

class ClientRegisteredEvent(
    val clientId: UUID,
    val email: String,
    val phoneNumber: String
) : DomainEvent() {
    override val eventName: String = "client.registered"
}

class ClientActivatedEvent(
    val clientId: UUID,
    val email: String,
    val keycloakId: String
) : DomainEvent() {
    override val eventName: String = "client.activated"
}

class ClientVerificationCodeRegeneratedEvent(
    val clientId: UUID,
    val phoneNumber: String
) : DomainEvent() {
    override val eventName: String = "client.verification_code_regenerated"
}