package com.dominikdev.booking.identity.application

import com.dominikdev.booking.identity.ClientRegistrationRequest
import com.dominikdev.booking.identity.CreateBusinessOwnerRequest
import com.dominikdev.booking.identity.CreateEmployeeAccountRequest
import com.dominikdev.booking.identity.UpdateProfileRequest
import com.dominikdev.booking.identity.domain.IdentityProvider
import com.dominikdev.booking.identity.domain.UserProfileRepository
import com.dominikdev.booking.identity.domain.*
import org.springframework.transaction.annotation.Transactional
import java.util.*

class IdentityApplicationService(
    private val userProfileRepository: UserProfileRepository,
    private val identityProvider: IdentityProvider
) {
    fun createBusinessOwner(request: CreateBusinessOwnerRequest): UserProfile {
        // Validate email not already in use
        if (userProfileRepository.existsByEmail(request.email)) {
            throw DuplicateUserException(request.email)
        }

        // Create user in Keycloak first
        val keycloakId = identityProvider.createBusinessOwnerUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            temporaryPassword = request.temporaryPassword,
            businessId = request.businessId
        )

        // Create profile in our database
        val userProfile = UserProfile.createBusinessOwner(
            keycloakId = keycloakId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            businessId = request.businessId
        )

        val savedProfile = userProfileRepository.save(userProfile)

        // Send welcome notification (fire-and-forget)
//        notificationsFacade.sendWelcomeMessage(
//            WelcomeNotification(
//                recipientId = savedProfile.id,
//                recipientEmail = savedProfile.email,
//                recipientName = savedProfile.getFullName(),
//                userType = "Business Owner",
//                businessName = null
//            )
//        )

        return savedProfile
    }

    fun createEmployeeAccount(request: CreateEmployeeAccountRequest): UserProfile {
        // Validate email not already in use
        if (userProfileRepository.existsByEmail(request.email)) {
            throw DuplicateUserException(request.email)
        }

        // Create user in Keycloak first
        val keycloakId = identityProvider.createEmployeeUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            temporaryPassword = request.temporaryPassword,
            businessId = request.businessId
        )

        // Create profile in our database
        val userProfile = UserProfile.createEmployee(
            keycloakId = keycloakId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            businessId = request.businessId
        )

        val savedProfile = userProfileRepository.save(userProfile)

//        // Send welcome notification (fire-and-forget)
//        notificationsFacade.sendWelcomeMessage(
//            WelcomeNotification(
//                recipientId = savedProfile.id,
//                recipientEmail = savedProfile.email,
//                recipientName = savedProfile.getFullName(),
//                userType = "Employee",
//                businessName = request.businessName
//            )
//        )

        return savedProfile
    }

    fun registerClient(request: ClientRegistrationRequest): UserProfile {
        // Validate email not already in use
        if (userProfileRepository.existsByEmail(request.email)) {
            throw DuplicateUserException(request.email)
        }

        // Create user in Keycloak first
        val keycloakId = identityProvider.createClientUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = request.password
        )

        // Create profile in our database
        val userProfile = UserProfile.createClient(
            keycloakId = keycloakId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber
        )

        val savedProfile = userProfileRepository.save(userProfile)

        // Send welcome notification (fire-and-forget)
//        notificationsFacade.sendWelcomeMessage(
//            WelcomeNotification(
//                recipientId = savedProfile.id,
//                recipientEmail = savedProfile.email,
//                recipientName = savedProfile.getFullName(),
//                userType = "Client",
//                businessName = null
//            )
//        )

        return savedProfile
    }

    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserProfile {
        val userProfile = userProfileRepository.findById(userId)
            ?: throw UserNotFoundException(userId.toString())

        val updatedProfile = userProfile.updateProfile(
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber
        )

        return userProfileRepository.save(updatedProfile)
    }

    fun deactivateUser(userId: UUID) {
        val userProfile = userProfileRepository.findById(userId)
            ?: throw UserNotFoundException(userId.toString())

        // Deactivate in Keycloak
        identityProvider.deactivateUser(userProfile.keycloakId)

        // Deactivate in our database
        val deactivatedProfile = userProfile.deactivate()
        userProfileRepository.save(deactivatedProfile)
    }

    @Transactional(readOnly = true)
    fun getUserProfile(userId: UUID): UserProfile? {
        return userProfileRepository.findById(userId)
    }

    @Transactional(readOnly = true)
    fun getUserProfileByKeycloakId(keycloakId: String): UserProfile? {
        return userProfileRepository.findByKeycloakId(keycloakId)
    }

    @Transactional(readOnly = true)
    fun getUserProfileByEmail(email: String): UserProfile? {
        return userProfileRepository.findByEmail(email)
    }

    @Transactional(readOnly = true)
    fun getBusinessUsers(businessId: UUID): List<UserProfile> {
        return userProfileRepository.findByBusinessId(businessId)
    }

    fun requestPasswordReset(email: String) {
        // Delegate to Keycloak for password reset flow
        identityProvider.sendPasswordResetEmail(email)
    }
}