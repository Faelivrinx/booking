package com.dominikdev.booking.shared.event

import java.time.LocalDateTime
import java.util.UUID

abstract class DomainEvent(
    val eventId: UUID = UUID.randomUUID(),
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    abstract val eventName: String
}