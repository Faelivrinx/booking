package com.dominikdev.booking.application.dto

import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class BusinessDTO(
    val id: UUID,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)