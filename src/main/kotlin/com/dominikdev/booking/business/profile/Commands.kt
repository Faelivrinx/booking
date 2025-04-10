package com.dominikdev.booking.business.profile

import java.time.LocalDateTime
import java.util.*

data class CreateBusinessProfileCommand(
    val name: String,
    val description: String?,
    val street: String,
    val city: String,
    val state: String?,
    val postalCode: String,
    // Owner details
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String?,
    val ownerPassword: String
)

data class UpdateBusinessProfileCommand(
    val name: String,
    val description: String?,
    val street: String,
    val city: String,
    val state: String?,
    val postalCode: String,
)

data class BusinessProfileDTO(
    val id: UUID,
    val name: String,
    val description: String?,
    val address: AddressDTO,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AddressDTO(
    val street: String,
    val city: String,
    val state: String?,
    val postalCode: String,
)