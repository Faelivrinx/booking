package com.dominikdev.booking.business.application.command

data class CreateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val initialPassword: String  // Added for Keycloak account creation
)