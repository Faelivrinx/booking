package com.dominikdev.booking.business.service

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a service offered by a business
 */
data class ServiceDTO(
    val id: UUID,
    val businessId: UUID,
    val name: String,
    val durationMinutes: Int,
    val description: String?,
    val price: BigDecimal?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)