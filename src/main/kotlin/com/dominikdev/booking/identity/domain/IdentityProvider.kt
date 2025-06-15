package com.dominikdev.booking.identity.domain

import java.util.*

interface IdentityProvider {
    // User Creation
    fun createBusinessOwnerUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        temporaryPassword: String,
        businessId: UUID
    ): String

    fun createEmployeeUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        temporaryPassword: String,
        businessId: UUID
    ): String

    fun createClientUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        password: String
    ): String

    // User Retrieval
    fun getUserByKeycloakId(keycloakId: String): UserProfile?
    fun getUserByEmail(email: String): UserProfile?
    fun getUsersByBusinessId(businessId: UUID): List<UserProfile>

    // User Operations
    fun updateUser(
        keycloakId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?
    ): UserProfile

    fun deactivateUser(keycloakId: String)
    fun activateUser(keycloakId: String)
    fun updateUserBusinessId(keycloakId: String, businessId: UUID)

    // Password Management
    fun updateUserPassword(keycloakId: String, newPassword: String)
    fun sendPasswordResetEmail(email: String)

    // Role Management
    fun getUserRoles(keycloakId: String): List<UserRole>
    fun hasRole(keycloakId: String, role: UserRole): Boolean
    fun assignRole(keycloakId: String, role: UserRole)
    fun removeRole(keycloakId: String, role: UserRole)

    // Permission Checking
    fun hasPermission(keycloakId: String, permission: Permission, businessId: UUID? = null): Boolean
}