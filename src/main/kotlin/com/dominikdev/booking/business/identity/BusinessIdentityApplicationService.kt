package com.dominikdev.booking.business.identity

import com.dominikdev.booking.shared.infrastructure.identity.IdentityManagementService
import com.dominikdev.booking.shared.values.Email
import com.dominikdev.booking.shared.values.Name
import com.dominikdev.booking.shared.values.PhoneNumber
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BusinessIdentityApplicationService(
    private val businessRepository: BusinessIdentityJpaRepository,
    private val userManagementPort: IdentityManagementService
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun createBusinessIdentity(command: CreateBusinessIdentityCommand): BusinessIdentityDTO {
        val email = Email.of(command.email)
        if (businessRepository.findByEmail(email.value) != null) {
            throw BusinessDomainException("Business with email ${command.email} already exists")
        }

        // Generate a business identity ID
        val businessIdentityId = BusinessIdentityId.generate()

        // Create the Keycloak user with the business ID
        val userId = try {
            userManagementPort.createBusinessUser(
                email = command.email,
                name = command.name,
                phone = command.phoneNumber,
                password = command.initialPassword,
                businessId = command.businessId
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to create Keycloak user for business: ${e.message}" }
            throw e
        }

        // Create business identity with the Keycloak ID and business ID
        val businessIdentity = BusinessIdentity.create(
            id = businessIdentityId,
            keycloakId = userId,
            name = Name.of(command.name),
            email = email,
            phoneNumber = PhoneNumber.ofNullable(command.phoneNumber),
            businessId = command.businessId
        )

        val savedBusinessIdentity = businessRepository.save(businessIdentity)
        return mapToDTO(savedBusinessIdentity)
    }

    @Transactional(readOnly = true)
    fun getBusinessById(businessId: UUID): BusinessIdentityDTO {
        val business = businessRepository.findById(businessId).orElseThrow { throw BusinessDomainException("Business not found for id: $businessId") }

        return mapToDTO(business)
    }

    @Transactional(readOnly = true)
    fun getBusinessByKeycloakId(keycloakId: String): BusinessIdentityDTO {
        val business = businessRepository.findByKeycloakId(keycloakId)
            ?: throw BusinessDomainException("Business not found for keycloakId: $keycloakId")

        return mapToDTO(business)
    }

    @Transactional
    fun updateBusiness(businessId: UUID, command: UpdateBusinessCommand): BusinessIdentityDTO {
        val business = businessRepository.findById(businessId).orElseThrow { throw BusinessDomainException("Business not found for id: $businessId") }

        val newEmail = Email.of(command.email)
        if (business.email != newEmail.value) {
            businessRepository.findByEmail(newEmail.value)?.let {
                if (it.id != business.id) {
                    throw BusinessDomainException("Business with email ${command.email} already exists")
                }
            }
        }

        business.updateBusinessInfo(
            name = Name.of(command.name),
            email = newEmail,
            phoneNumber = PhoneNumber.ofNullable(command.phoneNumber)
        )

        val updatedBusiness = businessRepository.save(business)
        return mapToDTO(updatedBusiness)
    }

    private fun mapToDTO(businessIdentity: BusinessIdentity): BusinessIdentityDTO {
        return BusinessIdentityDTO(
            id = businessIdentity.id,
            name = businessIdentity.name,
            email = businessIdentity.email,
            phoneNumber = businessIdentity.phoneNumber,
            createdAt = businessIdentity.createdAt,
            updatedAt = businessIdentity.updatedAt
        )
    }
}