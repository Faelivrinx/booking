package com.dominikdev.booking.appointment.domain.services

import java.util.*

data class ServiceInfo(
    val id: UUID,
    val name: String,
    val durationMinutes: Int,
    val description: String?,
    val price: java.math.BigDecimal?
)