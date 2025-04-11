package com.dominikdev.booking.shared.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.*


/**
 * Security evaluator that determines if the authenticated user is the owner
 * of a specific business based on Keycloak token information.
 */
@Component
class BusinessOwnerSecurityEvaluator {

    private val logger = KotlinLogging.logger {}

    /**
     * Checks if the authenticated user is the owner of the specified business.
     *
     * @param authentication The authentication token
     * @param businessId The ID of the business to check
     * @return true if the user is the owner, false otherwise
     */
    fun isBusinessOwner(authentication: Authentication, businessId: String): Boolean {
        // Handle null authentication
        if (authentication == null) {
            logger.warn { "Authentication is null during business ownership check" }
            return false
        }

        // We expect a JWT token for OAuth2 Resource Server
        if (authentication !is JwtAuthenticationToken) {
            logger.warn { "Authentication is not a JwtAuthenticationToken: ${authentication.javaClass.name}" }
            return false
        }

        // Get the token
        val token = authentication.token

        // Extract the business ID from the token
        val tokenBusinessId = extractBusinessIdFromToken(token)
        if (tokenBusinessId == null) {
            logger.warn { "No business_id found in token for user" }
            return false
        }

        // Compare business IDs
        try {
            val requestedBusinessId = UUID.fromString(businessId)
            val userBusinessId = UUID.fromString(tokenBusinessId)

            return requestedBusinessId == userBusinessId
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to parse business IDs for comparison" }
            return false
        }
    }

    /**
     * Extracts the business ID from the JWT token.
     *
     * @param token The JWT token
     * @return The business ID or null if not found
     */
    private fun extractBusinessIdFromToken(token: Jwt): String? {
        // First, check for business_id in the normal claims
        val businessId = token.claims["business_id"] as? String
        if (!businessId.isNullOrBlank()) {
            return businessId
        }

        // Then check in the attributes (common practice in Keycloak)
        @Suppress("UNCHECKED_CAST")
        val attributes = token.claims["attributes"] as? Map<String, Any>
        if (attributes != null) {
            @Suppress("UNCHECKED_CAST")
            val businessIdList = attributes["business_id"] as? List<String>
            if (!businessIdList.isNullOrEmpty()) {
                return businessIdList[0]
            }
        }

        // Finally, check in the resource access section
        @Suppress("UNCHECKED_CAST")
        val resourceAccess = token.claims["resource_access"] as? Map<String, Any>
        if (resourceAccess != null) {
            for ((_, accessData) in resourceAccess) {
                if (accessData is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val metadata = accessData["metadata"] as? Map<String, Any>
                    if (metadata != null && metadata.containsKey("business_id")) {
                        return metadata["business_id"].toString()
                    }
                }
            }
        }

        return null
    }
}
