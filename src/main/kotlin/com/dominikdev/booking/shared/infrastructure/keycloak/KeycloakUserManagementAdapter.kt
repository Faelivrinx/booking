package com.dominikdev.booking.shared.infrastructure.keycloak

import com.dominikdev.booking.business.profile.application.port.out.UserManagementPort
import com.dominikdev.booking.business.profile.domain.BusinessDomainException
import com.dominikdev.booking.clients.ClientDomainException
import com.dominikdev.booking.shared.exception.DomainException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class KeycloakUserManagementAdapter(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}") private val realm: String
) : UserManagementPort {

    private val logger = KotlinLogging.logger {}

    // User role constants
    companion object {
        const val ROLE_BUSINESS = "BUSINESS"
        const val ROLE_CLIENT = "CLIENT"
    }

    override fun createBusinessUser(
        email: String,
        name: String,
        phone: String?,
        password: String
    ): String {
        return createUser(email, name, phone, password, ROLE_BUSINESS, ::BusinessDomainException)
    }

        fun createClientUser(
        email: String,
        name: String,
        phone: String?,
        password: String
    ): String {
        return createUser(email, name, phone, password, ROLE_CLIENT, ::ClientDomainException)
    }

    override fun deleteUser(userId: String) {
        try {
            keycloak.realm(realm).users().delete(userId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete user from Keycloak: ${e.message}" }
            throw BusinessDomainException("Failed to delete user account", e)
        }
    }

    /**
     * Generic method to create a user with a specific role
     *
     * @param email User email
     * @param name User full name
     * @param phone User phone number
     * @param password User password
     * @param role Role to assign to the user
     * @param exceptionFactory Function to create domain-specific exceptions
     * @return Keycloak user ID
     */
    private fun <T : DomainException> createUser(
        email: String,
        name: String,
        phone: String?,
        password: String,
        role: String,
        exceptionFactory: (String, Throwable?) -> T
    ): String {
        try {
            val user = createUserRepresentation(email, name, phone, password)

            // Create user in Keycloak
            val response = keycloak.realm(realm).users().create(user)

            if (response.status != Response.Status.CREATED.statusCode) {
                logger.error { "Failed to create user in Keycloak. Status: ${response.status}" }
                throw exceptionFactory("Failed to create user account", null)
            }

            // Extract user ID from location URL in response
            val userId = extractUserIdFromResponse(response)

            // Assign the specified role to the user
            assignRole(userId, role)

            return userId
        } catch (e: WebApplicationException) {
            logger.error(e) { "WebApplicationException during user creation: ${e.message}" }
            if (e.response.status == Response.Status.CONFLICT.statusCode) {
                throw exceptionFactory("Email address already in use", null)
            }
            throw exceptionFactory("Failed to create user: ${e.message}", e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during user creation: ${e.message}" }
            throw exceptionFactory("Failed to create user account", e)
        }
    }

    private fun createUserRepresentation(
        email: String,
        name: String,
        phone: String?,
        password: String
    ): UserRepresentation {
        val nameParts = name.split(" ", limit = 2)
        val firstName = nameParts[0]
        val lastName = if (nameParts.size > 1) nameParts[1] else ""

        val user = UserRepresentation()
        user.isEnabled = true
        user.username = email
        user.email = email
        user.firstName = firstName
        user.lastName = lastName

        // Add phone number as attribute if provided
        if (!phone.isNullOrBlank()) {
            user.attributes = mapOf("phoneNumber" to listOf(phone))
        }

        // Set password credential
        val credential = CredentialRepresentation()
        credential.type = CredentialRepresentation.PASSWORD
        credential.value = password
        credential.isTemporary = false

        user.credentials = listOf(credential)

        return user
    }

    private fun extractUserIdFromResponse(response: Response): String {
        val locationPath = response.location?.path
        return locationPath?.substringAfterLast('/')
            ?: throw BusinessDomainException("Could not extract user ID from response")
    }

    private fun assignRole(userId: String, roleName: String) {
        try {
            // Get role from realm
            val role = keycloak.realm(realm).roles().get(roleName).toRepresentation()

            // Assign role to user
            keycloak.realm(realm).users().get(userId)
                .roles()
                .realmLevel()
                .add(Collections.singletonList(role))
        } catch (e: Exception) {
            logger.error(e) { "Failed to assign $roleName role to user: ${e.message}" }
            // We don't need to throw an exception here, as the user is already created
            // but we should log the error
        }
    }
}