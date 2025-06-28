package com.dominikdev.booking.identity

import com.dominikdev.booking.identity.domain.Permission
import com.dominikdev.booking.identity.domain.UserProfile
import com.dominikdev.booking.identity.domain.UserRole
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDateTime
import java.util.*

interface IdentityFacade {
    // Business Owner Management
    fun createBusinessOwner(request: CreateBusinessOwnerRequest): UserAccount
    fun getBusinessOwner(keycloakId: String): UserAccount?

    // Employee Account Management
    fun createEmployeeAccount(request: CreateEmployeeAccountRequest): UserAccount
    fun getEmployeeAccount(keycloakId: String): UserAccount?
    fun deactivateEmployeeAccount(keycloakId: String)

    // Client Management
    fun registerClient(request: ClientRegistrationRequest): ClientRegistrationResult
    fun getClientAccount(keycloakId: String): UserAccount?

    // Profile Management
    fun updateProfile(keycloakId: String, request: UpdateProfileRequest): UserAccount
    fun requestPasswordReset(email: String)

    // Authorization
    fun getUserRoles(keycloakId: String): List<UserRole>
    fun hasPermission(keycloakId: String, permission: Permission, businessId: UUID? = null): Boolean

    // JWT Integration
    fun extractUserAttributes(): UserAttributes
    fun extractUserAttributes(jwt: Jwt): UserAttributes

    // Helper methods for current user (from JWT context)
    fun getCurrentUserProfile(): UserProfile?
    fun getCurrentKeycloakId(): String?
    fun getCurrentUserAccount(): UserAccount?
}

// Domain DTOs
data class UserAccount(
    val keycloakId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val roles: List<UserRole>,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: LocalDateTime
) {
    fun getFullName(): String = "$firstName $lastName"
}

data class ClientRegistrationResult(
    val keycloakId: String,
    val verificationRequired: Boolean,
    val verificationToken: String?
)

data class EmployeeCreationResult(
    val userProfile: UserProfile,
)

// Request DTOs
data class CreateBusinessOwnerRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val temporaryPassword: String,
    val businessId: UUID
)

data class CreateEmployeeAccountRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val businessId: UUID,
    val temporaryPassword: String
)

data class ClientRegistrationRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val password: String
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?
)

data class PasswordResetRequest(
    val email: String
)

data class UserAttributes(
    val keycloakId: String,
    val businessId: UUID?,
    val role: UserRole?,
    val email: String?
) {
    companion object {
        fun empty(keycloakId: String) = UserAttributes(
            keycloakId = keycloakId,
            businessId = null,
            role = null,
            email = null
        )
    }
}