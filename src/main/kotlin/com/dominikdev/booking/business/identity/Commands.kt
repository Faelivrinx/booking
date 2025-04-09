package com.dominikdev.booking.business.identity

data class CreateBusinessIdentityCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val initialPassword: String  // Added for Keycloak account creation
)

data class UpdateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?
)