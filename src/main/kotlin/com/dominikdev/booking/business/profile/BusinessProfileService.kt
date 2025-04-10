package com.dominikdev.booking.business.profile

import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BusinessProfileService(
    private val businessProfileRepository: BusinessProfileRepository,
    private val businessIdentityPort: BusinessIdentityAdapter,
    private val eventPublisher: DomainEventPublisher
) {

    private val logger = KotlinLogging.logger {}

    /**
     * Creates a new business with owner account
     */
    @Transactional
    fun createBusinessWithOwner(command: CreateBusinessProfileCommand): BusinessProfileDTO {
        // Create address embeddable
        val address = Address(
            street = command.street,
            city = command.city,
            state = command.state,
            postalCode = command.postalCode,
        )

        // Create the business entity
        val businessEntity = BusinessProfileEntity(
            name = command.name,
            description = command.description,
            address = address
        )

        // Save the business to get the ID
        val savedBusiness = businessProfileRepository.save(businessEntity)

        try {
            // Create the business identity (user account)
            businessIdentityPort.createBusinessIdentity(
                businessId = savedBusiness.id,
                name = command.ownerName,
                email = command.ownerEmail,
                phoneNumber = command.ownerPhone,
                initialPassword = command.ownerPassword
            )

            // Publish business created event
            eventPublisher.publish(
                BusinessProfileCreatedEvent(
                    businessId = savedBusiness.id,
                    businessName = savedBusiness.name
                )
            )

            return mapToDTO(savedBusiness)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create business owner account: ${e.message}" }
            throw BusinessProfileException("Failed to create business: ${e.message}", e)
        }
    }

    /**
     * Gets a business by ID
     */
    @Transactional(readOnly = true)
    fun getBusinessById(id: UUID): BusinessProfileDTO {
        val business = businessProfileRepository.findById(id)
            .orElseThrow { BusinessProfileException("Business not found with ID: $id") }

        return mapToDTO(business)
    }

    /**
     * Updates an existing business
     */
    @Transactional
    fun updateBusiness(id: UUID, command: UpdateBusinessProfileCommand): BusinessProfileDTO {
        val business = businessProfileRepository.findById(id)
            .orElseThrow { BusinessProfileException("Business not found with ID: $id") }

        val updatedAddress = Address(
            street = command.street,
            city = command.city,
            state = command.state,
            postalCode = command.postalCode,
        )

        business.update(
            name = command.name,
            description = command.description,
            address = updatedAddress
        )

        val updatedBusiness = businessProfileRepository.save(business)

        eventPublisher.publish(
            BusinessProfileUpdatedEvent(
                businessId = updatedBusiness.id,
                businessName = updatedBusiness.name
            )
        )

        return mapToDTO(updatedBusiness)
    }

    /**
     * Maps a business entity to DTO
     */
    private fun mapToDTO(business: BusinessProfileEntity): BusinessProfileDTO {
        return BusinessProfileDTO(
            id = business.id,
            name = business.name,
            description = business.description,
            address = AddressDTO(
                street = business.address.street,
                city = business.address.city,
                state = business.address.state,
                postalCode = business.address.postalCode,
            ),
            createdAt = business.createdAt,
            updatedAt = business.updatedAt
        )
    }
}