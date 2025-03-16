package com.dominikdev.booking.business.application.dto

import java.time.LocalDateTime
import java.util.UUID

data class BusinessDTO(
    val id: UUID,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)