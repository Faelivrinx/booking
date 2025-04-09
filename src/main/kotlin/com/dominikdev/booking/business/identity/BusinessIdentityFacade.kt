package com.dominikdev.booking.business.identity

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BusinessIdentityFacade(private val businessApplicationService: BusinessIdentityApplicationService) {

    /**
     * Creates a new business with the provided information.
     * @param command The command containing business creation details
     * @return The created business data
     */
    fun createBusinessIdentity(command: CreateBusinessIdentityCommand): BusinessIdentityDTO {
        return businessApplicationService.createBusinessIdentity(command)
    }

    /**
     * Retrieves a business by its ID.
     * @param businessId The unique identifier of the business
     * @return The business data
     */
    fun getBusinessById(businessId: UUID): BusinessIdentityDTO {
        return businessApplicationService.getBusinessById(businessId)
    }

    /**
     * Retrieves a business by its Keycloak user ID.
     * @param keycloakId The Keycloak user ID associated with the business
     * @return The business data
     */
    fun getBusinessByKeycloakId(keycloakId: String): BusinessIdentityDTO {
        return businessApplicationService.getBusinessByKeycloakId(keycloakId)
    }

    /**
     * Updates an existing business with new information.
     * @param businessId The business ID to update
     * @param command The command containing updated business details
     * @return The updated business data
     */
    fun updateBusiness(businessId: UUID, command: UpdateBusinessCommand): BusinessIdentityDTO {
        return businessApplicationService.updateBusiness(businessId, command)
    }
}