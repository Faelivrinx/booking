package com.dominikdev.booking.offer.domain


import java.time.LocalDateTime
import java.util.*

data class Business(
    val id: UUID,
    val name: String,
    val description: String?,
    val address: Address,
    val ownerId: String, // Keycloak ID
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun updateProfile(
        name: String,
        description: String?,
        address: Address
    ): Business {
        return this.copy(
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            address = address,
            updatedAt = LocalDateTime.now()
        )
    }

    fun deactivate(): Business {
        return copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
    }
}