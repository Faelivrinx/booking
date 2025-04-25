package com.dominikdev.booking.shared.infrastructure.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SecurityContextUtils {
    /**
     * Extracts the business ID from the current security context.
     * @return The business ID UUID or null if not found
     */
    fun extractBusinessId(): UUID? {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is JwtAuthenticationToken) {
            val token = authentication.token
            val businessIdStr = extractBusinessIdFromToken(token)

            if (!businessIdStr.isNullOrBlank()) {
                return try {
                    UUID.fromString(businessIdStr)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }

        return null
    }

    /**
     * Gets the business ID from the context or throws an exception if not found.
     * @return The business ID UUID
     * @throws BusinessSecurityException if business ID is not found
     */
    fun getBusinessIdOrThrow(): UUID {
        return extractBusinessId() ?: throw BusinessSecurityException("Business ID not found in authentication token")
    }

    /**
     * Extracts the business ID from the JWT token.
     * @param token The JWT token
     * @return The business ID string or null if not found
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