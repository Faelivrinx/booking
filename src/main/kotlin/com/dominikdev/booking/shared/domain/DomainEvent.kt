package com.dominikdev.booking.shared.domain

import java.time.LocalDateTime
import java.util.*

abstract class DomainEvent(
    val eventId: UUID = UUID.randomUUID(),
    val occurredAt: LocalDateTime = LocalDateTime.now()
)