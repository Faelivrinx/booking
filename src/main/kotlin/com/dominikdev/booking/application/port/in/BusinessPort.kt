package com.dominikdev.booking.application.port.`in`

import com.dominikdev.booking.application.command.CreateBusinessCommand
import com.dominikdev.booking.application.command.UpdateBusinessCommand
import com.dominikdev.booking.application.dto.BusinessDTO
import java.util.UUID

/**
 * Port interface for business operations.
 * This defines the operations that can be performed on the business bounded context.
 */
interface BusinessPort {
    fun createBusiness(command: CreateBusinessCommand): BusinessDTO
    fun getBusinessById(businessId: UUID): BusinessDTO
    fun getBusinessByKeycloakId(keycloakId: String): BusinessDTO
    fun updateBusiness(businessId: UUID, command: UpdateBusinessCommand): BusinessDTO
}