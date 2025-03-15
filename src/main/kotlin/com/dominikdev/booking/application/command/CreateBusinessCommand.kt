package com.dominikdev.booking.application.command

import java.time.LocalTime

data class CreateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val openingTime: LocalTime?,
    val closingTime: LocalTime?,
    val initialPassword: String  // Added for Keycloak account creation
)