package com.dominikdev.booking.application.service

import com.dominikdev.booking.application.command.CreateBusinessCommand
import com.dominikdev.booking.application.dto.BusinessDTO
import com.dominikdev.booking.application.port.out.UserManagementPort
import com.dominikdev.booking.domain.business.Business
import com.dominikdev.booking.domain.business.BusinessRepository
import com.dominikdev.booking.domain.shared.Email
import com.dominikdev.booking.domain.shared.Name
import com.dominikdev.booking.domain.shared.PhoneNumber
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class BusinessApplicationService(
    private val businessRepository: BusinessRepository,
    private val userManagementPort: UserManagementPort
) {
    private val logger = KotlinLogging.logger {}

//        @Transactional
    fun createBusiness(command: CreateBusinessCommand): BusinessDTO {
        val userId = userManagementPort.createBusinessUser(
            email = command.email,
            name = command.name,
            phone = command.phoneNumber,
            password = command.initialPassword
        )

        val business = Business.create(
            keycloakId = userId,
            name = Name.of(command.name),
            email = Email.of(command.email),
            phoneNumber = PhoneNumber.ofNullable(command.phoneNumber),
            openingTime = command.openingTime,
            closingTime = command.closingTime
        )
        try {
            // Save business
            val savedBusiness = businessRepository.save(business)
            return mapToDTO(savedBusiness)
        } catch (e: Exception) {
            try {
                userManagementPort.deleteUser(userId)
            } catch (ex: Exception) {
                logger.error("Failed to delete user $userId after business creation failure", ex)
            }
            throw e
        }
    }

    private fun mapToDTO(business: Business): BusinessDTO {
        return BusinessDTO(
            id = business.id.value,
            name = business.getName().value,
            email = business.getEmail().value,
            phoneNumber = business.getPhoneNumber()?.value,
            openingTime = business.getOpeningTime(),
            closingTime = business.getClosingTime(),
            createdAt = business.createdAt,
            updatedAt = business.updatedAt
        )
    }
}