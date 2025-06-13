package com.dominikdev.booking.identity.domain

import com.dominikdev.booking.shared.domain.DomainException
import java.time.LocalDateTime
import java.util.*

/**
 * Domain entity representing user profile information stored in our database
 * Keycloak handles authentication, this handles additional business data
 */
data class UserProfile(
    val id: UUID,
    val keycloakId: String, // Links to Keycloak user
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val businessId: UUID?, // Only for BUSINESS_OWNER and EMPLOYEE
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun createBusinessOwner(
            keycloakId: String,
            email: String,
            firstName: String,
            lastName: String,
            phoneNumber: String?,
            businessId: UUID
        ): UserProfile {
            return UserProfile(
                id = UUID.randomUUID(),
                keycloakId = keycloakId,
                email = email.lowercase().trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber?.trim(),
                role = UserRole.BUSINESS_OWNER,
                businessId = businessId,
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }

        fun createEmployee(
            keycloakId: String,
            email: String,
            firstName: String,
            lastName: String,
            phoneNumber: String?,
            businessId: UUID
        ): UserProfile {
            return UserProfile(
                id = UUID.randomUUID(),
                keycloakId = keycloakId,
                email = email.lowercase().trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber?.trim(),
                role = UserRole.EMPLOYEE,
                businessId = businessId,
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }

        fun createClient(
            keycloakId: String,
            email: String,
            firstName: String,
            lastName: String,
            phoneNumber: String?
        ): UserProfile {
            return UserProfile(
                id = UUID.randomUUID(),
                keycloakId = keycloakId,
                email = email.lowercase().trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber?.trim(),
                role = UserRole.CLIENT,
                businessId = null,
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }

    fun updateProfile(firstName: String, lastName: String, phoneNumber: String?): UserProfile {
        return this.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phoneNumber = phoneNumber?.trim(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun deactivate(): UserProfile {
        return this.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
    }

    fun getFullName(): String = "$firstName $lastName"
}

enum class UserRole {
    BUSINESS_OWNER, EMPLOYEE, CLIENT, ADMIN
}

enum class Permission {
    MANAGE_BUSINESS, MANAGE_EMPLOYEES, MANAGE_SERVICES,
    MANAGE_SCHEDULE, VIEW_RESERVATIONS, MANAGE_RESERVATIONS,
    VIEW_OWN_SCHEDULE, REQUEST_SCHEDULE_CHANGES
}

open class IdentityException(message: String, cause: Throwable? = null) : DomainException(message, cause)
class UserNotFoundException(userId: String) : IdentityException("User not found: $userId")
class DuplicateUserException(email: String) : IdentityException("User with email $email already exists")