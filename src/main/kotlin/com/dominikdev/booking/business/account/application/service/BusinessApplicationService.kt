package com.dominikdev.booking.business.account.application.service

import com.dominikdev.booking.business.account.application.command.CreateBusinessCommand
import com.dominikdev.booking.business.account.application.command.UpdateBusinessCommand
import com.dominikdev.booking.business.account.application.dto.BusinessDTO
import com.dominikdev.booking.business.account.application.port.out.UserManagementPort
import com.dominikdev.booking.business.account.domain.Business
import com.dominikdev.booking.business.account.domain.BusinessDomainException
import com.dominikdev.booking.business.account.domain.BusinessId
import com.dominikdev.booking.business.account.domain.BusinessRepository
import com.dominikdev.booking.shared.values.Email
import com.dominikdev.booking.shared.values.Name
import com.dominikdev.booking.shared.values.PhoneNumber
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BusinessApplicationService(
    private val businessRepository: BusinessRepository,
    private val userManagementPort: UserManagementPort
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun createBusiness(command: CreateBusinessCommand): BusinessDTO {
        val email = Email.of(command.email)
        if (businessRepository.findByEmail(email) != null) {
            throw BusinessDomainException("Business with email ${command.email} already exists")
        }

        // Generate a business ID first
        val businessId = BusinessId.generate()

        // Create the Keycloak user with the business ID
        val userId = try {
            userManagementPort.createBusinessUser(
                email = command.email,
                name = command.name,
                phone = command.phoneNumber,
                password = command.initialPassword,
                businessId = businessId.toString()
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to create Keycloak user for business: ${e.message}" }
            throw e
        }

        // Create business with the generated ID and Keycloak ID
        val business = Business.create(
            id = businessId,
            keycloakId = userId,
            name = Name.of(command.name),
            email = email,
            phoneNumber = PhoneNumber.ofNullable(command.phoneNumber)
        )

        try {
            val savedBusiness = businessRepository.save(business)
            return mapToDTO(savedBusiness)
        } catch (e: Exception) {
            try {
                userManagementPort.deleteUser(userId)
            } catch (ex: Exception) {
                logger.error(ex) { "Failed to delete user $userId after business creation failure" }
            }
            throw e
        }
    }

    @Transactional(readOnly = true)
    fun getBusinessById(businessId: UUID): BusinessDTO {
        val business = businessRepository.findById(BusinessId.from(businessId))
            ?: throw BusinessDomainException("Business not found for id: $businessId")

        return mapToDTO(business)
    }

    @Transactional(readOnly = true)
    fun getBusinessByKeycloakId(keycloakId: String): BusinessDTO {
        val business = businessRepository.findByKeycloakId(keycloakId)
            ?: throw BusinessDomainException("Business not found for keycloakId: $keycloakId")

        return mapToDTO(business)
    }

    @Transactional
    fun updateBusiness(businessId: UUID, command: UpdateBusinessCommand): BusinessDTO {
        val business = businessRepository.findById(BusinessId.from(businessId))
            ?: throw BusinessDomainException("Business not found for id: $businessId")

        val newEmail = Email.of(command.email)
        if (business.getEmail().value != newEmail.value) {
            businessRepository.findByEmail(newEmail)?.let {
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

    private fun mapToDTO(business: Business): BusinessDTO {
        return BusinessDTO(
            id = business.id.value,
            name = business.getName().value,
            email = business.getEmail().value,
            phoneNumber = business.getPhoneNumber()?.value,
            createdAt = business.createdAt,
            updatedAt = business.updatedAt
        )
    }
}