package com.dominikdev.booking.offer.domain


import java.time.LocalDateTime
import java.util.*

data class StaffMember(
    val id: UUID,
    val businessId: UUID,
    val keycloakId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val jobTitle: String?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    init {
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(keycloakId.isNotBlank()) { "Keycloak ID cannot be blank" }
    }

    fun getFullName(): String = "$firstName $lastName"

    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        jobTitle: String?
    ): StaffMember {
        return this.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phoneNumber = phoneNumber?.trim()?.takeIf { it.isNotEmpty() },
            jobTitle = jobTitle?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = LocalDateTime.now()
        )
    }

    fun deactivate(): StaffMember {
        return copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
    }
}