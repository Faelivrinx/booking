package com.dominikdev.booking.identity

import com.dominikdev.booking.identity.domain.Permission
import com.dominikdev.booking.identity.domain.UserRole
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDateTime
import java.util.*

interface IdentityFacade {
    fun createBusinessOwner(request: CreateBusinessOwnerRequest): UserAccount
    fun getBusinessOwner(userId: UUID): UserAccount?

    // Employee Account Management
    fun createEmployeeAccount(request: CreateEmployeeAccountRequest): UserAccount
    fun getEmployeeAccount(userId: UUID): UserAccount?
    fun deactivateEmployeeAccount(userId: UUID)

    // Client Management
    fun registerClient(request: ClientRegistrationRequest): ClientRegistrationResult
    fun verifyClientEmail(token: String): UserAccount
    fun getClientAccount(userId: UUID): UserAccount?

    // Authentication
    fun authenticate(email: String, password: String): AuthenticationResult
    fun refreshToken(refreshToken: String): AuthenticationResult
    fun logout(userId: UUID)

    // Profile Management
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserAccount
    fun changePassword(userId: UUID, currentPassword: String, newPassword: String)
    fun requestPasswordReset(email: String)
    fun resetPassword(token: String, newPassword: String)

    // Authorization
    fun getUserRoles(userId: UUID): List<UserRole>
    fun hasPermission(userId: UUID, permission: Permission): Boolean

    fun extractUserAttributes(): UserAttributes
    fun extractUserAttributes(jwt: Jwt): UserAttributes
}

// Domain DTOs
data class UserAccount(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val roles: List<UserRole>,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: LocalDateTime
)

data class AuthenticationResult(
    val userId: UUID,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val userRoles: List<UserRole>
)

data class ClientRegistrationResult(
    val userId: UUID,
    val verificationRequired: Boolean,
    val verificationToken: String?
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

data class UserAttributes(
    val userId: UUID?,
    val businessId: UUID?,
    val role: UserRole?,
    val email: String?,
    val keycloakId: String
) {
    companion object {
        fun empty(keycloakId: String) = UserAttributes(
            userId = null,
            businessId = null,
            role = null,
            email = null,
            keycloakId = keycloakId
        )
    }
}