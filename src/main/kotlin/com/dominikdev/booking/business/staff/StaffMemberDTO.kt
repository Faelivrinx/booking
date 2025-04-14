package com.dominikdev.booking.business.staff

import java.time.LocalDateTime
import java.util.UUID

data class StaffMemberDTO(
    val id: UUID,
    val businessId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val jobTitle: String?,
    val active: Boolean,
    val activatedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)