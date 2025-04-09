package com.dominikdev.booking.business.identity

import java.time.LocalDateTime
import java.util.UUID

data class BusinessIdentityDTO(
    val id: UUID,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)