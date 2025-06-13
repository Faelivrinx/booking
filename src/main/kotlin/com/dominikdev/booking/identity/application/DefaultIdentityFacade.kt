package com.dominikdev.booking.identity.application

import com.dominikdev.booking.identity.*
import com.dominikdev.booking.identity.application.*
import com.dominikdev.booking.identity.domain.IdentityException
import com.dominikdev.booking.identity.domain.Permission
import com.dominikdev.booking.identity.domain.UserProfile
import com.dominikdev.booking.identity.domain.UserRole
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*

class DefaultIdentityFacade(
    private val identityApplicationService: IdentityApplicationService
) : IdentityFacade {

    override fun createBusinessOwner(request: CreateBusinessOwnerRequest): UserAccount {
        val userProfile = identityApplicationService.createBusinessOwner(
            CreateBusinessOwnerRequest(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                temporaryPassword = request.temporaryPassword,
                businessId = UUID.randomUUID() // Will be created in Offer context
            )
        )
        return mapToUserAccount(userProfile)
    }

    override fun getBusinessOwner(userId: UUID): UserAccount? {
        return identityApplicationService.getUserProfile(userId)?.let { mapToUserAccount(it) }
    }

    override fun createEmployeeAccount(request: CreateEmployeeAccountRequest): UserAccount {
        val userProfile = identityApplicationService.createEmployeeAccount(
            CreateEmployeeAccountRequest(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                businessId = request.businessId,
                temporaryPassword = request.temporaryPassword
            )
        )
        return mapToUserAccount(userProfile)
    }

    override fun getEmployeeAccount(userId: UUID): UserAccount? {
        return identityApplicationService.getUserProfile(userId)?.let { mapToUserAccount(it) }
    }

    override fun deactivateEmployeeAccount(userId: UUID) {
        identityApplicationService.deactivateUser(userId)
    }

    override fun registerClient(request: ClientRegistrationRequest): ClientRegistrationResult {
        val userProfile = identityApplicationService.registerClient(
            ClientRegistrationRequest(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                password = request.password
            )
        )

        return ClientRegistrationResult(
            userId = userProfile.id,
            verificationRequired = false, // Keycloak handles email verification
            verificationToken = null
        )
    }

    override fun verifyClientEmail(token: String): UserAccount {
        // Keycloak handles email verification
        throw UnsupportedOperationException("Email verification is handled by Keycloak")
    }

    override fun getClientAccount(userId: UUID): UserAccount? {
        return identityApplicationService.getUserProfile(userId)?.let { mapToUserAccount(it) }
    }

    override fun authenticate(email: String, password: String): AuthenticationResult {
        // Authentication is handled by Keycloak/Spring Security
        throw UnsupportedOperationException("Authentication is handled by Keycloak")
    }

    override fun refreshToken(refreshToken: String): AuthenticationResult {
        // Token refresh is handled by Keycloak/Spring Security
        throw UnsupportedOperationException("Token refresh is handled by Keycloak")
    }

    override fun logout(userId: UUID) {
        // Logout is handled by Keycloak/Spring Security
        throw UnsupportedOperationException("Logout is handled by Keycloak")
    }

    override fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserAccount {
        val userProfile = identityApplicationService.updateProfile(
            userId,
            UpdateProfileRequest(
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber
            )
        )
        return mapToUserAccount(userProfile)
    }

    override fun changePassword(userId: UUID, currentPassword: String, newPassword: String) {
        // Password change is handled by Keycloak
        throw UnsupportedOperationException("Password change is handled by Keycloak")
    }

    override fun requestPasswordReset(email: String) {
        identityApplicationService.requestPasswordReset(email)
    }

    override fun resetPassword(token: String, newPassword: String) {
        // Password reset is handled by Keycloak
        throw UnsupportedOperationException("Password reset is handled by Keycloak")
    }

    override fun getUserRoles(userId: UUID): List<UserRole> {
        val userProfile = identityApplicationService.getUserProfile(userId)
        return userProfile?.let { listOf(mapToFacadeRole(it.role)) } ?: emptyList()
    }

    override fun hasPermission(userId: UUID, permission: Permission): Boolean {
        val userProfile = identityApplicationService.getUserProfile(userId) ?: return false

        return when (userProfile.role) {
            UserRole.ADMIN -> true
            UserRole.BUSINESS_OWNER -> {
                permission in listOf(
                    Permission.MANAGE_BUSINESS, Permission.MANAGE_EMPLOYEES,
                    Permission.MANAGE_SERVICES, Permission.MANAGE_SCHEDULE,
                    Permission.VIEW_RESERVATIONS, Permission.MANAGE_RESERVATIONS
                )
            }
            UserRole.EMPLOYEE -> {
                permission in listOf(
                    Permission.VIEW_OWN_SCHEDULE, Permission.REQUEST_SCHEDULE_CHANGES,
                    Permission.VIEW_RESERVATIONS
                )
            }
            UserRole.CLIENT -> false
        }
    }

    override fun extractUserAttributes(): UserAttributes {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return extractUserAttributes(authentication.token)
        }
        throw IdentityException("No valid JWT token found in security context")
    }

    override fun extractUserAttributes(jwt: Jwt): UserAttributes {
        val keycloakId = jwt.subject

        // Try to get user profile from database
        val userProfile = identityApplicationService.getUserProfileByKeycloakId(keycloakId)

        if (userProfile != null) {
            // User exists in our database - return complete info
            return UserAttributes(
                userId = userProfile.id,
                businessId = userProfile.businessId,
                role = mapToFacadeRole(userProfile.role),
                email = userProfile.email,
                keycloakId = keycloakId
            )
        } else {
            // User not in our database yet - extract what we can from JWT
            val businessId = extractBusinessIdFromJwt(jwt)
            val email = jwt.claims["email"] as? String
            val role = extractRoleFromJwt(jwt)

            return UserAttributes(
                userId = null,
                businessId = businessId,
                role = role,
                email = email,
                keycloakId = keycloakId
            )
        }
    }
    // Helper method to get current user from JWT
    fun getCurrentUserProfile(): UserProfile? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            val jwt = authentication.token
            val keycloakId = jwt.subject
            return identityApplicationService.getUserProfileByKeycloakId(keycloakId)
        }
        return null
    }

    private fun mapToUserAccount(userProfile: com.dominikdev.booking.identity.domain.UserProfile): UserAccount {
        return UserAccount(
            id = userProfile.id,
            email = userProfile.email,
            firstName = userProfile.firstName,
            lastName = userProfile.lastName,
            phoneNumber = userProfile.phoneNumber,
            roles = listOf(mapToFacadeRole(userProfile.role)),
            isActive = userProfile.isActive,
            isEmailVerified = true, // Keycloak handles verification
            createdAt = userProfile.createdAt
        )
    }

    private fun mapToFacadeRole(domainRole: UserRole): UserRole {
        return when (domainRole) {
            UserRole.BUSINESS_OWNER -> UserRole.BUSINESS_OWNER
            UserRole.EMPLOYEE -> UserRole.EMPLOYEE
            UserRole.CLIENT -> UserRole.CLIENT
            UserRole.ADMIN -> UserRole.ADMIN
        }
    }

    private fun extractBusinessIdFromJwt(jwt: Jwt): UUID? {
        // Check in custom attributes
        val attributes = jwt.claims["attributes"] as? Map<*, *>
        val businessIdFromAttributes = attributes?.get("business_id") as? String

        // Check in custom claim
        val businessIdFromClaim = jwt.claims["business_id"] as? String

        val businessIdStr = businessIdFromAttributes ?: businessIdFromClaim

        return try {
            businessIdStr?.let { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun extractRoleFromJwt(jwt: Jwt): UserRole? {
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val realmRoles = realmAccess?.get("roles") as? List<*>

        return realmRoles?.filterIsInstance<String>()?.firstNotNullOfOrNull { roleName ->
            when (roleName) {
                "BUSINESS_OWNER" -> UserRole.BUSINESS_OWNER
                "EMPLOYEE" -> UserRole.EMPLOYEE
                "CLIENT" -> UserRole.CLIENT
                "ADMIN" -> UserRole.ADMIN
                else -> null
            }
        }
    }
}