package com.dominikdev.booking.business.staff

import java.util.UUID

data class CreateStaffRequest(
    val businessId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val jobTitle: String?,
    val businessName: String // For notification purposes
)

data class UpdateStaffProfileRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val jobTitle: String?
)