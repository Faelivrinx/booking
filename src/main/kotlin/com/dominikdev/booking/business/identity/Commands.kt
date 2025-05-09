package com.dominikdev.booking.business.identity

import java.util.*

data class CreateBusinessIdentityCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val initialPassword: String,  // Added for Keycloak account creation
    val businessId: UUID
)

data class UpdateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?
)