package com.dominikdev.booking.identity.application

import com.dominikdev.booking.identity.ClientRegistrationRequest
import com.dominikdev.booking.identity.CreateBusinessOwnerRequest
import com.dominikdev.booking.identity.CreateEmployeeAccountRequest
import com.dominikdev.booking.identity.UpdateProfileRequest
import com.dominikdev.booking.identity.domain.IdentityProvider
import com.dominikdev.booking.identity.domain.*
import org.springframework.transaction.annotation.Transactional
import java.util.*

open class IdentityApplicationService(
    private val identityProvider: IdentityProvider
) {
    fun createBusinessOwner(request: CreateBusinessOwnerRequest): UserProfile {
        validateEmail(request.email)

        val keycloakId = identityProvider.createBusinessOwnerUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            temporaryPassword = request.temporaryPassword,
            businessId = request.businessId
        )

        return identityProvider.getUserByKeycloakId(keycloakId)
            ?: throw IdentityException("Failed to retrieve created business owner")
    }

    fun createEmployeeAccount(request: CreateEmployeeAccountRequest): UserProfile {
        validateEmail(request.email)

        val keycloakId = identityProvider.createEmployeeUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            temporaryPassword = request.temporaryPassword,
            businessId = request.businessId
        )

        return identityProvider.getUserByKeycloakId(keycloakId)
            ?: throw IdentityException("Failed to retrieve created employee")
    }

    fun registerClient(request: ClientRegistrationRequest): UserProfile {
        validateEmail(request.email)
        validatePassword(request.password)

        val keycloakId = identityProvider.createClientUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            password = request.password
        )

        return identityProvider.getUserByKeycloakId(keycloakId)
            ?: throw IdentityException("Failed to retrieve created client")
    }

    fun updateProfile(keycloakId: String, request: UpdateProfileRequest): UserProfile {
        return identityProvider.updateUser(
            keycloakId = keycloakId,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber
        )
    }

    fun deactivateUser(keycloakId: String) {
        identityProvider.deactivateUser(keycloakId)
    }

    fun activateUser(keycloakId: String) {
        identityProvider.activateUser(keycloakId)
    }

    @Transactional(readOnly = true)
    fun getUserProfile(keycloakId: String): UserProfile? {
        return identityProvider.getUserByKeycloakId(keycloakId)
    }

    @Transactional(readOnly = true)
    fun getUserProfileByEmail(email: String): UserProfile? {
        return identityProvider.getUserByEmail(email)
    }

    @Transactional(readOnly = true)
    fun getBusinessUsers(businessId: UUID): List<UserProfile> {
        return identityProvider.getUsersByBusinessId(businessId)
    }

    fun requestPasswordReset(email: String) {
        identityProvider.sendPasswordResetEmail(email)
    }

    fun assignUserToBusiness(keycloakId: String, businessId: UUID) {
        identityProvider.updateUserBusinessId(keycloakId, businessId)
    }

    @Transactional(readOnly = true)
    fun hasPermission(keycloakId: String, permission: Permission, businessId: UUID? = null): Boolean {
        return identityProvider.hasPermission(keycloakId, permission, businessId)
    }

    @Transactional(readOnly = true)
    fun getUserRoles(keycloakId: String): List<UserRole> {
        return identityProvider.getUserRoles(keycloakId)
    }

    private fun validateEmail(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            throw InvalidUserDataException("Invalid email format")
        }

        // Check if user already exists
        identityProvider.getUserByEmail(email)?.let {
            throw DuplicateUserException(email)
        }
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw InvalidUserDataException("Password must be at least 8 characters long")
        }
    }
}