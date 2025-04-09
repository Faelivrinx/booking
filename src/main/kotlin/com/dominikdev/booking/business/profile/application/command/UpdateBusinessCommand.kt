package com.dominikdev.booking.business.profile.application.command

data class UpdateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?
)