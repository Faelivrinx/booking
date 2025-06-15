package com.dominikdev.booking.identity.application

import com.dominikdev.booking.identity.*
import com.dominikdev.booking.identity.domain.*
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.LocalDateTime
import java.util.*

/**
 * Default implementation of IdentityFacade.
 * This class serves as the main entry point for all identity operations,
 * coordinating between the application service and domain layer.
 */
class DefaultIdentityFacade(
    private val identityApplicationService: IdentityApplicationService,
    private val identityProvider: IdentityProvider
) : IdentityFacade {

    private val logger = LoggerFactory.getLogger(DefaultIdentityFacade::class.java)

    // Business Owner Management
    override fun createBusinessOwner(request: CreateBusinessOwnerRequest): UserAccount {
        logger.info("Creating business owner: ${request.email}")

        val userProfile = identityApplicationService.createBusinessOwner(request)
        return mapToUserAccount(userProfile).also {
            logger.info("Successfully created business owner: ${it.keycloakId}")
        }
    }

    override fun getBusinessOwner(keycloakId: String): UserAccount? {
        logger.debug("Retrieving business owner: $keycloakId")

        return identityApplicationService.getUserProfile(keycloakId)
            ?.takeIf { it.role == UserRole.BUSINESS_OWNER }
            ?.let { mapToUserAccount(it) }
    }

    // Employee Account Management
    override fun createEmployeeAccount(request: CreateEmployeeAccountRequest): UserAccount {
        logger.info("Creating employee account: ${request.email}")

        val userProfile = identityApplicationService.createEmployeeAccount(request)
        return mapToUserAccount(userProfile).also {
            logger.info("Successfully created employee: ${it.keycloakId}")
        }
    }

    override fun getEmployeeAccount(keycloakId: String): UserAccount? {
        logger.debug("Retrieving employee account: $keycloakId")

        return identityApplicationService.getUserProfile(keycloakId)
            ?.takeIf { it.role == UserRole.EMPLOYEE }
            ?.let { mapToUserAccount(it) }
    }

    override fun deactivateEmployeeAccount(keycloakId: String) {
        logger.info("Deactivating employee account: $keycloakId")

        // Verify the user is actually an employee
        val userProfile = identityApplicationService.getUserProfile(keycloakId)
        if (userProfile?.role != UserRole.EMPLOYEE) {
            throw IdentityException("User is not an employee or does not exist")
        }

        identityApplicationService.deactivateUser(keycloakId)
        logger.info("Successfully deactivated employee: $keycloakId")
    }

    // Client Management
    override fun registerClient(request: ClientRegistrationRequest): ClientRegistrationResult {
        logger.info("Registering client: ${request.email}")

        val userProfile = identityApplicationService.registerClient(request)

        return ClientRegistrationResult(
            keycloakId = userProfile.keycloakId,
            verificationRequired = false, // Keycloak handles email verification
            verificationToken = null
        ).also {
            logger.info("Successfully registered client: ${it.keycloakId}")
        }
    }

    override fun getClientAccount(keycloakId: String): UserAccount? {
        logger.debug("Retrieving client account: $keycloakId")

        return identityApplicationService.getUserProfile(keycloakId)
            ?.takeIf { it.role == UserRole.CLIENT }
            ?.let { mapToUserAccount(it) }
    }

    // Profile Management
    override fun updateProfile(keycloakId: String, request: UpdateProfileRequest): UserAccount {
        logger.info("Updating profile for user: $keycloakId")

        val userProfile = identityApplicationService.updateProfile(keycloakId, request)
        return mapToUserAccount(userProfile).also {
            logger.info("Successfully updated profile for user: $keycloakId")
        }
    }

    override fun requestPasswordReset(email: String) {
        logger.info("Requesting password reset for email: $email")

        try {
            identityApplicationService.requestPasswordReset(email)
            logger.info("Password reset email sent to: $email")
        } catch (e: Exception) {
            // Log the actual error but don't expose it for security reasons
            logger.warn("Password reset request failed for email: $email", e)
            // Don't throw - we want to return success regardless for security
        }
    }

    // Authorization
    override fun getUserRoles(keycloakId: String): List<UserRole> {
        logger.debug("Getting roles for user: $keycloakId")
        return identityApplicationService.getUserRoles(keycloakId)
    }

    override fun hasPermission(keycloakId: String, permission: Permission, businessId: UUID?): Boolean {
        logger.debug("Checking permission $permission for user: $keycloakId in business: $businessId")

        return identityApplicationService.hasPermission(keycloakId, permission, businessId).also { hasAccess ->
            logger.debug("Permission check result: $hasAccess for user: $keycloakId, permission: $permission")
        }
    }

    // JWT Integration
    override fun extractUserAttributes(): UserAttributes {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return extractUserAttributes(authentication.token)
        }
        throw IdentityException("No valid JWT token found in security context")
    }

    override fun extractUserAttributes(jwt: Jwt): UserAttributes {
        val keycloakId = jwt.subject
        logger.debug("Extracting user attributes for: $keycloakId")

        val userProfile = identityApplicationService.getUserProfile(keycloakId)

        return if (userProfile != null) {
            // User exists in Keycloak - return complete info
            UserAttributes(
                keycloakId = keycloakId,
                businessId = userProfile.businessId,
                role = userProfile.role,
                email = userProfile.email
            )
        } else {
            // User not found in Keycloak - fallback to JWT claims
            logger.warn("User not found in Keycloak, falling back to JWT claims: $keycloakId")
            UserAttributes(
                keycloakId = keycloakId,
                businessId = extractBusinessIdFromJwt(jwt),
                role = extractRoleFromJwt(jwt),
                email = jwt.claims["email"] as? String
            )
        }
    }

    // Helper methods for current user (from JWT context)
    override fun getCurrentUserProfile(): UserProfile? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            val keycloakId = authentication.token.subject
            return identityApplicationService.getUserProfile(keycloakId)
        }
        return null
    }

    override fun getCurrentKeycloakId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is JwtAuthenticationToken) {
            authentication.token.subject
        } else {
            null
        }
    }

    override fun getCurrentUserAccount(): UserAccount? {
        return getCurrentUserProfile()?.let { userProfile ->
            mapToUserAccount(userProfile)
        }
    }

    // Additional helper methods for business context validation

    /**
     * Validates that the current user has access to the specified business context.
     * @param businessId The business ID to validate access for
     * @param requiredPermission Optional permission that must be held for the business
     * @throws UnauthorizedException if access is denied
     */
    fun validateBusinessAccess(businessId: UUID, requiredPermission: Permission? = null) {
        val currentUser = getCurrentUserProfile()
            ?: throw UnauthorizedException("No authenticated user found")

        // Admin users have access to all businesses
        if (currentUser.role == UserRole.ADMIN) {
            return
        }

        // Business owners and employees must belong to the business
        if (currentUser.businessId != businessId) {
            throw UnauthorizedException("User does not have access to business: $businessId")
        }

        // Check specific permission if required
        if (requiredPermission != null) {
            val hasPermission = hasPermission(currentUser.keycloakId, requiredPermission, businessId)
            if (!hasPermission) {
                throw UnauthorizedException("User lacks required permission: $requiredPermission")
            }
        }
    }

    /**
     * Gets all users associated with a specific business.
     * Only accessible by business owners, admins, or users within the same business.
     */
    fun getBusinessUsers(businessId: UUID): List<UserAccount> {
        logger.info("Getting users for business: $businessId")

        // Validate access
        validateBusinessAccess(businessId, Permission.MANAGE_EMPLOYEES)

        return identityApplicationService.getBusinessUsers(businessId)
            .map { mapToUserAccount(it) }
            .also { users ->
                logger.info("Retrieved ${users.size} users for business: $businessId")
            }
    }

    /**
     * Assigns a user to a business. Only accessible by admins or business owners.
     */
    fun assignUserToBusiness(keycloakId: String, businessId: UUID) {
        logger.info("Assigning user $keycloakId to business: $businessId")

        val currentUser = getCurrentUserProfile()
            ?: throw UnauthorizedException("No authenticated user found")

        // Only admins or business owners can assign users
        if (currentUser.role != UserRole.ADMIN &&
            !(currentUser.role == UserRole.BUSINESS_OWNER && currentUser.businessId == businessId)) {
            throw UnauthorizedException("Insufficient permissions to assign users to business")
        }

        identityApplicationService.assignUserToBusiness(keycloakId, businessId)
        logger.info("Successfully assigned user $keycloakId to business: $businessId")
    }

    // Private helper methods

    private fun mapToUserAccount(userProfile: UserProfile): UserAccount {
        return UserAccount(
            keycloakId = userProfile.keycloakId,
            email = userProfile.email,
            firstName = userProfile.firstName,
            lastName = userProfile.lastName,
            phoneNumber = userProfile.phoneNumber,
            roles = listOf(userProfile.role),
            isActive = userProfile.isActive,
            isEmailVerified = true, // Keycloak handles email verification
            createdAt = userProfile.createdAt ?: LocalDateTime.now()
        )
    }

    private fun extractBusinessIdFromJwt(jwt: Jwt): UUID? {
        // Try different JWT claim locations for business ID
        val businessIdStr = jwt.claims["business_id"] as? String
            ?: (jwt.claims["attributes"] as? Map<*, *>)?.get("business_id") as? String

        return try {
            businessIdStr?.let { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid business_id format in JWT: $businessIdStr")
            null
        }
    }

    private fun extractRoleFromJwt(jwt: Jwt): UserRole? {
        // Extract roles from realm_access claim
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val realmRoles = realmAccess?.get("roles") as? List<*>

        // Return the highest priority role found
        val roleHierarchy = listOf(
            "ADMIN" to UserRole.ADMIN,
            "BUSINESS_OWNER" to UserRole.BUSINESS_OWNER,
            "EMPLOYEE" to UserRole.EMPLOYEE,
            "CLIENT" to UserRole.CLIENT
        )

        return roleHierarchy.firstOrNull { (roleName, _) ->
            realmRoles?.contains(roleName) == true
        }?.second
    }
}