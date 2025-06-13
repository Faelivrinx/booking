package com.dominikdev.booking.identity.infrastructure

import com.dominikdev.booking.identity.domain.IdentityException
import com.dominikdev.booking.identity.domain.IdentityProvider
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import java.util.*

class DefaultIdentityProvider(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}") private val realm: String
) : IdentityProvider {
    override fun createBusinessOwnerUser(
        email: String,
        firstName: String,
        lastName: String,
        temporaryPassword: String,
        businessId: UUID
    ): String {
        return createKeycloakUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = temporaryPassword,
            roles = listOf("BUSINESS_OWNER"),
            attributes = mapOf("business_id" to listOf(businessId.toString()))
        )
    }

    override fun createEmployeeUser(
        email: String,
        firstName: String,
        lastName: String,
        temporaryPassword: String,
        businessId: UUID
    ): String {
        return createKeycloakUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = temporaryPassword,
            roles = listOf("EMPLOYEE"),
            attributes = mapOf("business_id" to listOf(businessId.toString()))
        )
    }

    override fun createClientUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): String {
        return createKeycloakUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password,
            roles = listOf("CLIENT"),
            attributes = emptyMap()
        )
    }

    override fun deactivateUser(keycloakId: String) {
        try {
            val user = keycloak.realm(realm).users().get(keycloakId).toRepresentation()
            user.isEnabled = false
            keycloak.realm(realm).users().get(keycloakId).update(user)
        } catch (e: Exception) {
            throw IdentityException("Failed to deactivate user in Keycloak: ${e.message}", e)
        }
    }

    override fun updateUserPassword(keycloakId: String, newPassword: String) {
        try {
            val credential = CredentialRepresentation()
            credential.type = CredentialRepresentation.PASSWORD
            credential.value = newPassword
            credential.isTemporary = false

            keycloak.realm(realm).users().get(keycloakId).resetPassword(credential)
        } catch (e: Exception) {
            throw IdentityException("Failed to update password in Keycloak: ${e.message}", e)
        }
    }

    override fun sendPasswordResetEmail(email: String) {
        try {
            // Find user by email
            val users = keycloak.realm(realm).users().search(email)
            val user = users.firstOrNull { it.email == email }
                ?: throw IdentityException("User not found with email: $email")

            // Send password reset email
            keycloak.realm(realm).users().get(user.id).executeActionsEmail(
                listOf("UPDATE_PASSWORD")
            )
        } catch (e: Exception) {
            throw IdentityException("Failed to send password reset email: ${e.message}", e)
        }
    }

    private fun createKeycloakUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        roles: List<String>,
        attributes: Map<String, List<String>>
    ): String {
        try {
            val user = UserRepresentation()
            user.username = email
            user.email = email
            user.firstName = firstName
            user.lastName = lastName
            user.isEnabled = true
            user.isEmailVerified = true // Set to true for business users
            user.attributes = attributes

            // Set password
            val credential = CredentialRepresentation()
            credential.type = CredentialRepresentation.PASSWORD
            credential.value = password
            credential.isTemporary = roles.contains("BUSINESS_OWNER") || roles.contains("EMPLOYEE") // Force password change for business users
            user.credentials = listOf(credential)

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

            return userId
        } catch (e: Exception) {
            throw IdentityException("Failed to create user in Keycloak: ${e.message}", e)
        }
    }

    private fun assignRoles(userId: String, roles: List<String>) {
        try {
            val realmRoles = roles.mapNotNull { roleName ->
                try {
                    keycloak.realm(realm).roles().get(roleName).toRepresentation()
                } catch (e: Exception) {
                    null // Role doesn't exist, skip it
                }
            }

            if (realmRoles.isNotEmpty()) {
                keycloak.realm(realm).users().get(userId)
                    .roles().realmLevel().add(realmRoles)
            }
        } catch (e: Exception) {
            // Log warning but don't fail - user is created, just roles might not be assigned
            println("Warning: Failed to assign roles to user $userId: ${e.message}")
        }
    }
}