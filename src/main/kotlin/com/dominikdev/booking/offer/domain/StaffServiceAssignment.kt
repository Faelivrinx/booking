package com.dominikdev.booking.offer.domain

import java.time.LocalDateTime
import java.util.*

data class StaffServiceAssignment(
    val employeeId: UUID,
    val serviceId: UUID,
    val businessId: UUID,
    val assignedAt: LocalDateTime = LocalDateTime.now()
)