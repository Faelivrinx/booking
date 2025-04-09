package com.dominikdev.booking.business.account.application.command

data class UpdateBusinessCommand(
    val name: String,
    val email: String,
    val phoneNumber: String?
)