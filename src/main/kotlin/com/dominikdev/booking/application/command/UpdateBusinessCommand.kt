package com.dominikdev.booking.application.command

data class UpdateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?
)