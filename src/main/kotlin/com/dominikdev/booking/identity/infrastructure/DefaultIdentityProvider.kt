package com.dominikdev.booking.identity.infrastructure

import com.dominikdev.booking.identity.domain.*
import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

open class DefaultIdentityProvider(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}") private val realm: String

    ) : IdentityProvider {
    private val logger = LoggerFactory.getLogger(DefaultIdentityProvider::class.java)

    override fun createBusinessOwnerUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        temporaryPassword: String,
        businessId: UUID
    ): String {
        logger.info("Creating business owner user: $email")

        return createKeycloakUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            password = temporaryPassword,
            roles = listOf("BUSINESS_OWNER"),
            attributes = buildUserAttributes(phoneNumber, businessId),
            isTemporary = true
        )
    }

    override fun createEmployeeUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        temporaryPassword: String,
        businessId: UUID
    ): String {
        logger.info("Creating employee user: $email")

        return createKeycloakUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            password = temporaryPassword,
            roles = listOf("EMPLOYEE"),
            attributes = buildUserAttributes(phoneNumber, businessId),
            isTemporary = true
        )
    }

    override fun createClientUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        password: String
    ): String {
        logger.info("Creating client user: $email")

        return createKeycloakUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            password = password,
            roles = listOf("CLIENT"),
            attributes = buildUserAttributes(phoneNumber, null),
            isTemporary = false
        )
    }

    override fun getUserByKeycloakId(keycloakId: String): UserProfile? {
        return try {
            val userResource = keycloak.realm(realm).users().get(keycloakId)
            val user = userResource.toRepresentation()
            mapToUserProfile(user, userResource)
        } catch (e: NotFoundException) {
            logger.warn("User not found with keycloakId: $keycloakId")
            null
        } catch (e: Exception) {
            logger.error("Error retrieving user by keycloakId: $keycloakId", e)
            null
        }
    }

    override fun getUserByEmail(email: String): UserProfile? {
        return try {
            val users = keycloak.realm(realm).users().search(email, true)
            val user = users.firstOrNull { it.email.equals(email, ignoreCase = true) }

            if (user != null) {
                val userResource = keycloak.realm(realm).users().get(user.id)
                mapToUserProfile(user, userResource)
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error retrieving user by email: $email", e)
            null
        }
    }

    override fun getUsersByBusinessId(businessId: UUID): List<UserProfile> {
        return try {
            val users = keycloak.realm(realm).users().list()
            users.filter { user ->
                user.attributes?.get("business_id")?.contains(businessId.toString()) == true
            }.mapNotNull { user ->
                try {
                    val userResource = keycloak.realm(realm).users().get(user.id)
                    mapToUserProfile(user, userResource)
                } catch (e: Exception) {
                    logger.warn("Failed to map user: ${user.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Error getting business users for businessId: $businessId", e)
            throw IdentityException("Failed to get business users: ${e.message}", e)
        }
    }

    override fun updateUser(
        keycloakId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?
    ): UserProfile {
        return try {
            val userResource = keycloak.realm(realm).users().get(keycloakId)
            val user = userResource.toRepresentation()

            // Update basic information
            user.firstName = firstName.trim()
            user.lastName = lastName.trim()

            // Update phone number in attributes
            val attributes = user.attributes?.toMutableMap() ?: mutableMapOf()
            if (phoneNumber?.isNotBlank() == true) {
                attributes["phone_number"] = listOf(phoneNumber.trim())
            } else {
                attributes.remove("phone_number")
            }
            user.attributes = attributes

            // Save changes
            userResource.update(user)

            logger.info("Updated user: $keycloakId")

            // Return updated user profile
            mapToUserProfile(user, userResource)
                ?: throw IdentityException("Failed to retrieve updated user")
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error updating user: $keycloakId", e)
            throw IdentityException("Failed to update user in Keycloak: ${e.message}", e)
        }
    }

    override fun deactivateUser(keycloakId: String) {
        try {
            val userResource = keycloak.realm(realm).users().get(keycloakId)
            val user = userResource.toRepresentation()
            user.isEnabled = false
            userResource.update(user)

            logger.info("Deactivated user: $keycloakId")
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error deactivating user: $keycloakId", e)
            throw IdentityException("Failed to deactivate user in Keycloak: ${e.message}", e)
        }
    }

    override fun activateUser(keycloakId: String) {
        try {
            val userResource = keycloak.realm(realm).users().get(keycloakId)
            val user = userResource.toRepresentation()
            user.isEnabled = true
            userResource.update(user)

            logger.info("Activated user: $keycloakId")
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error activating user: $keycloakId", e)
            throw IdentityException("Failed to activate user in Keycloak: ${e.message}", e)
        }
    }

    override fun updateUserBusinessId(keycloakId: String, businessId: UUID) {
        try {
            val userResource = keycloak.realm(realm).users().get(keycloakId)
            val user = userResource.toRepresentation()

            val attributes = user.attributes?.toMutableMap() ?: mutableMapOf()
            attributes["business_id"] = listOf(businessId.toString())
            user.attributes = attributes

            userResource.update(user)

            logger.info("Updated business ID for user: $keycloakId to $businessId")
        } catch (_: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error updating user business ID: $keycloakId", e)
            throw IdentityException("Failed to update user business ID: ${e.message}", e)
        }
    }

    override fun updateUserPassword(keycloakId: String, newPassword: String) {
        try {
            val credential = CredentialRepresentation().apply {
                type = CredentialRepresentation.PASSWORD
                value = newPassword
                isTemporary = false
            }

            keycloak.realm(realm).users().get(keycloakId).resetPassword(credential)

            logger.info("Updated password for user: $keycloakId")
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error updating password for user: $keycloakId", e)
            throw IdentityException("Failed to update password in Keycloak: ${e.message}", e)
        }
    }

    override fun sendPasswordResetEmail(email: String) {
        try {
            val users = keycloak.realm(realm).users().search(email, true)
            val user = users.firstOrNull { it.email.equals(email, ignoreCase = true) }
                ?: throw UserNotFoundException(email)

            keycloak.realm(realm).users().get(user.id).executeActionsEmail(
                listOf("UPDATE_PASSWORD")
            )

            logger.info("Sent password reset email to: $email")
        } catch (e: UserNotFoundException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error sending password reset email to: $email", e)
            throw IdentityException("Failed to send password reset email: ${e.message}", e)
        }
    }

    override fun getUserRoles(keycloakId: String): List<UserRole> {
        return try {
            val userResource = keycloak.realm(realm).users().get(keycloakId)
            val realmRoles = userResource.roles().realmLevel().listAll()

            realmRoles.mapNotNull { role ->
                when (role.name) {
                    "BUSINESS_OWNER" -> UserRole.BUSINESS_OWNER
                    "EMPLOYEE" -> UserRole.EMPLOYEE
                    "CLIENT" -> UserRole.CLIENT
                    "ADMIN" -> UserRole.ADMIN
                    else -> null
                }
            }
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error getting user roles for: $keycloakId", e)
            throw IdentityException("Failed to get user roles: ${e.message}", e)
        }
    }

    override fun hasRole(keycloakId: String, role: UserRole): Boolean {
        return getUserRoles(keycloakId).contains(role)
    }

    override fun assignRole(keycloakId: String, role: UserRole) {
        try {
            val roleName = role.name
            val realmRole = keycloak.realm(realm).roles().get(roleName).toRepresentation()
            keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().add(listOf(realmRole))

            logger.info("Assigned role $role to user: $keycloakId")
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error assigning role $role to user: $keycloakId", e)
            throw IdentityException("Failed to assign role: ${e.message}", e)
        }
    }

    override fun removeRole(keycloakId: String, role: UserRole) {
        try {
            val roleName = role.name
            val realmRole = keycloak.realm(realm).roles().get(roleName).toRepresentation()
            keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().remove(listOf(realmRole))

            logger.info("Removed role $role from user: $keycloakId")
        } catch (e: NotFoundException) {
            throw UserNotFoundException(keycloakId)
        } catch (e: Exception) {
            logger.error("Error removing role $role from user: $keycloakId", e)
            throw IdentityException("Failed to remove role: ${e.message}", e)
        }
    }

    override fun hasPermission(keycloakId: String, permission: Permission, businessId: UUID?): Boolean {
        val userProfile = getUserByKeycloakId(keycloakId) ?: return false

        // Business context validation for non-admin users
        if (businessId != null && userProfile.businessId != businessId && userProfile.role != UserRole.ADMIN) {
            return false
        }

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

    // Private helper methods

    private fun createKeycloakUser(
        email: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        password: String,
        roles: List<String>,
        attributes: Map<String, List<String>>,
        isTemporary: Boolean
    ): String {
        try {
            validateUserInput(email, firstName, lastName, password)

            val user = UserRepresentation().apply {
                username = email.lowercase().trim()
                this.email = email.lowercase().trim()
                this.firstName = firstName.trim()
                this.lastName = lastName.trim()
                isEnabled = true
                isEmailVerified = true
                this.attributes = attributes
                credentials = listOf(
                    CredentialRepresentation().apply {
                        type = CredentialRepresentation.PASSWORD
                        value = password
                        this.isTemporary = isTemporary
                    }
                )
            }

            // Create user in Keycloak
            val response = keycloak.realm(realm).users().create(user)
            if (response.status != 201) {
                throw IdentityException("Failed to create user in Keycloak. Status: ${response.status}")
            }

            // Extract user ID from location header
            val location = response.location.path
            val userId = location.substring(location.lastIndexOf('/') + 1)

            // Assign roles
            assignRoles(userId, roles)

            logger.info("Created user: $email with ID: $userId")
            return userId

        } catch (e: IdentityException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error creating user: $email", e)
            throw IdentityException("Failed to create user in Keycloak: ${e.message}", e)
        }
    }

    private fun assignRoles(userId: String, roles: List<String>) {
        try {
            val realmRoles = roles.mapNotNull { roleName ->
                try {
                    keycloak.realm(realm).roles().get(roleName).toRepresentation()
                } catch (e: Exception) {
                    logger.warn("Role '$roleName' not found, skipping assignment")
                    null
                }
            }

            if (realmRoles.isNotEmpty()) {
                keycloak.realm(realm).users().get(userId)
                    .roles().realmLevel().add(realmRoles)
                logger.debug("Assigned roles ${roles.joinToString()} to user: $userId")
            }
        } catch (e: Exception) {
            logger.warn("Warning: Failed to assign roles to user $userId: ${e.message}")
            // Don't fail user creation if role assignment fails
        }
    }

    private fun mapToUserProfile(user: UserRepresentation, userResource: UserResource): UserProfile? {
        return try {
            val attributes = user.attributes ?: emptyMap()
            val businessIdStr = attributes["business_id"]?.firstOrNull()
            val phoneNumber = attributes["phone_number"]?.firstOrNull()

            // Get user's primary role
            val role = extractUserRole(userResource) ?: run {
                logger.warn("No valid role found for user: ${user.id}")
                return null
            }

            UserProfile(
                keycloakId = user.id,
                email = user.email ?: "",
                firstName = user.firstName ?: "",
                lastName = user.lastName ?: "",
                phoneNumber = phoneNumber?.takeIf { it.isNotBlank() },
                role = role,
                businessId = businessIdStr?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        logger.warn("Invalid business_id format for user ${user.id}: $it")
                        null
                    }
                },
                isActive = user.isEnabled ?: true,
                createdAt = user.createdTimestamp?.let {
                    LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC)
                },
                updatedAt = null // Keycloak doesn't track last updated time by default
            )
        } catch (e: Exception) {
            logger.error("Error mapping user to profile: ${user.id}", e)
            null
        }
    }

    private fun extractUserRole(userResource: UserResource): UserRole? {
        return try {
            val realmRoles = userResource.roles().realmLevel().listAll()

            // Return the highest priority role found
            val roleHierarchy = listOf(
                "ADMIN" to UserRole.ADMIN,
                "BUSINESS_OWNER" to UserRole.BUSINESS_OWNER,
                "EMPLOYEE" to UserRole.EMPLOYEE,
                "CLIENT" to UserRole.CLIENT
            )

            roleHierarchy.firstOrNull { (roleName, _) ->
                realmRoles.any { it.name == roleName }
            }?.second

        } catch (e: Exception) {
            logger.warn("Failed to extract user role", e)
            null
        }
    }

    private fun buildUserAttributes(phoneNumber: String?, businessId: UUID?): Map<String, List<String>> {
        val attributes = mutableMapOf<String, List<String>>()

        phoneNumber?.takeIf { it.isNotBlank() }?.let {
            attributes["phone_number"] = listOf(it.trim())
        }

        businessId?.let {
            attributes["business_id"] = listOf(it.toString())
        }

        return attributes
    }

    private fun validateUserInput(email: String, firstName: String, lastName: String, password: String) {
        if (email.isBlank() || !email.contains("@")) {
            throw InvalidUserDataException("Invalid email format: $email")
        }

        if (firstName.isBlank()) {
            throw InvalidUserDataException("First name cannot be blank")
        }

        if (lastName.isBlank()) {
            throw InvalidUserDataException("Last name cannot be blank")
        }

        if (password.length < 6) {
            throw InvalidUserDataException("Password must be at least 6 characters long")
        }
    }
}