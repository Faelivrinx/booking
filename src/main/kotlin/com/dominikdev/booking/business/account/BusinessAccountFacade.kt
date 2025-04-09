package com.dominikdev.booking.business.account

import com.dominikdev.booking.business.account.application.command.CreateBusinessCommand
import com.dominikdev.booking.business.account.application.command.UpdateBusinessCommand
import com.dominikdev.booking.business.account.application.dto.BusinessDTO
import com.dominikdev.booking.business.account.application.port.`in`.BusinessPort
import com.dominikdev.booking.business.account.application.service.BusinessApplicationService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BusinessAccountFacade(private val businessApplicationService: BusinessApplicationService) : BusinessPort {

    /**
     * Creates a new business with the provided information.
     * @param command The command containing business creation details
     * @return The created business data
     */
    override fun createBusiness(command: CreateBusinessCommand): BusinessDTO {
        return businessApplicationService.createBusiness(command)
    }

    /**
     * Retrieves a business by its ID.
     * @param businessId The unique identifier of the business
     * @return The business data
     */
    override fun getBusinessById(businessId: UUID): BusinessDTO {
        return businessApplicationService.getBusinessById(businessId)
    }

    /**
     * Retrieves a business by its Keycloak user ID.
     * @param keycloakId The Keycloak user ID associated with the business
     * @return The business data
     */
    override fun getBusinessByKeycloakId(keycloakId: String): BusinessDTO {
        return businessApplicationService.getBusinessByKeycloakId(keycloakId)
    }

    /**
     * Updates an existing business with new information.
     * @param businessId The business ID to update
     * @param command The command containing updated business details
     * @return The updated business data
     */
    override fun updateBusiness(businessId: UUID, command: UpdateBusinessCommand): BusinessDTO {
        return businessApplicationService.updateBusiness(businessId, command)
    }
}