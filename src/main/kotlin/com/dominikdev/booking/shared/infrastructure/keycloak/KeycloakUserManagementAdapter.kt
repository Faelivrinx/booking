package com.dominikdev.booking.shared.infrastructure.keycloak

import com.dominikdev.booking.business.application.port.out.UserManagementPort
import com.dominikdev.booking.business.domain.BusinessDomainException
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

    override fun createBusinessUser(
        email: String,
        name: String,
        phone: String?,
        password: String
    ): String {
        try {
            val user = createUserRepresentation(email, name, phone, password)

            // Create user in Keycloak
            val response = keycloak.realm(realm).users().create(user)

            if (response.status != Response.Status.CREATED.statusCode) {
                logger.error { "Failed to create user in Keycloak. Status: ${response.status}" }
                throw BusinessDomainException("Failed to create business user account")
            }

            // Extract user ID from location URL in response
            val userId = extractUserIdFromResponse(response)

            // Assign business role to the user
            assignBusinessRole(userId)

            return userId
        } catch (e: WebApplicationException) {
            logger.error(e) { "WebApplicationException during user creation: ${e.message}" }
            if (e.response.status == Response.Status.CONFLICT.statusCode) {
                throw BusinessDomainException("Email address already in use")
            }
            throw BusinessDomainException("Failed to create business user: ${e.message}")
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during user creation: ${e.message}" }
            throw BusinessDomainException("Failed to create business user account", e)
        }
    }

    override fun deleteUser(userId: String) {
        try {
            keycloak.realm(realm).users().delete(userId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete user from Keycloak: ${e.message}" }
            throw BusinessDomainException("Failed to delete user account", e)
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

    private fun assignBusinessRole(userId: String) {
        try {
            // Get business role from realm
            val businessRole = keycloak.realm(realm).roles().get("BUSINESS").toRepresentation()

            // Assign role to user
            keycloak.realm(realm).users().get(userId)
                .roles()
                .realmLevel()
                .add(Collections.singletonList(businessRole))
        } catch (e: Exception) {
            logger.error(e) { "Failed to assign business role to user: ${e.message}" }
            // We don't need to throw an exception here, as the user is already created
            // but we should log the error
        }
    }
}