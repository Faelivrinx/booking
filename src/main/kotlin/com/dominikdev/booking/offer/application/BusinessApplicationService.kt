package com.dominikdev.booking.offer.application

import com.dominikdev.booking.identity.CreateBusinessOwnerRequest
import com.dominikdev.booking.identity.IdentityFacade
import com.dominikdev.booking.identity.domain.UserRole
import com.dominikdev.booking.offer.CreateBusinessRequest
import com.dominikdev.booking.offer.UpdateBusinessRequest
import com.dominikdev.booking.offer.domain.*
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

class BusinessApplicationService(
    private val businessRepository: BusinessRepository,
    private val identityFacade: IdentityFacade
) {

    private val logger = LoggerFactory.getLogger(BusinessApplicationService::class.java)

    @Transactional
    fun createBusiness(request: CreateBusinessRequest): Business {
        logger.info("Creating business: ${request.name}")

        // Validate current user doesn't already own a business
        val userAttributes = identityFacade.extractUserAttributes()
        if (userAttributes.role == UserRole.BUSINESS_OWNER && userAttributes.businessId != null) {
            throw InvalidBusinessOperationException("User already owns a business")
        }

        try {
            // Step 1: Create business owner in Identity context
            val businessId = UUID.randomUUID()
            val createOwnerRequest = CreateBusinessOwnerRequest(
                email = request.ownerEmail,
                firstName = request.ownerName.split(" ").first(),
                lastName = request.ownerName.split(" ").drop(1).joinToString(" ").ifEmpty { "Owner" },
                phoneNumber = request.ownerPhone,
                temporaryPassword = request.ownerPassword,
                businessId = businessId
            )

            val ownerAccount = identityFacade.createBusinessOwner(createOwnerRequest)
            logger.info("Created business owner: ${ownerAccount.keycloakId}")

            // Step 2: Create business
            val business = Business(
                id = businessId,
                name = request.name.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
                address = Address(
                    street = request.street.trim(),
                    city = request.city.trim(),
                    state = request.state.trim(),
                    postalCode = request.postalCode.trim()
                ),
                ownerId = ownerAccount.keycloakId,
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            val savedBusiness = businessRepository.save(business)
            logger.info("Successfully created business: ${savedBusiness.id}")

            return savedBusiness

        } catch (e: Exception) {
            logger.error("Failed to create business: ${request.name}", e)
            throw InvalidBusinessOperationException("Failed to create business: ${e.message}")
        }
    }

    @Transactional
    fun updateBusiness(businessId: UUID, request: UpdateBusinessRequest): Business {
        logger.info("Updating business: $businessId")

        // Validate business ownership
        validateBusinessOwnership(businessId)

        val existingBusiness = businessRepository.findById(businessId)
            ?: throw BusinessNotFoundException(businessId)

        if (!existingBusiness.isActive) {
            throw InvalidBusinessOperationException("Cannot update deactivated business")
        }

        val address = Address(
            street = request.street.trim(),
            city = request.city.trim(),
            state = request.state.trim(),
            postalCode = request.postalCode.trim()
        )

        val updatedBusiness = existingBusiness.updateProfile(
            name = request.name.trim(),
            description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            address = address
        )

        val saved = businessRepository.save(updatedBusiness)
        logger.info("Successfully updated business: $businessId")

        return saved
    }

    @Transactional(readOnly = true)
    fun getBusiness(businessId: UUID): Business? {
        logger.debug("Retrieving business: $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        return businessRepository.findById(businessId)
    }

    @Transactional(readOnly = true)
    fun getBusinessByOwnerId(ownerId: String): Business? {
        logger.debug("Retrieving business by owner: $ownerId")

        // Only allow if current user is the owner or admin
        val userAttributes = identityFacade.extractUserAttributes()
        if (userAttributes.role != UserRole.ADMIN && userAttributes.keycloakId != ownerId) {
            throw UnauthorizedBusinessAccessException("Cannot access other user's business")
        }

        return businessRepository.findByOwnerId(ownerId)
    }

    @Transactional(readOnly = true)
    fun getAllActiveBusinesses(): List<Business> {
        logger.debug("Retrieving all active businesses")

        // Only admins can see all businesses
        val userAttributes = identityFacade.extractUserAttributes()
        if (userAttributes.role != UserRole.ADMIN) {
            throw UnauthorizedBusinessAccessException("Only admins can view all businesses")
        }

        return businessRepository.findAllActive()
    }

    @Transactional
    fun deactivateBusiness(businessId: UUID) {
        logger.info("Deactivating business: $businessId")

        // Validate business ownership (only owner or admin can deactivate)
        validateBusinessOwnership(businessId)

        val business = businessRepository.findById(businessId)
            ?: throw BusinessNotFoundException(businessId)

        if (!business.isActive) {
            logger.warn("Business already deactivated: $businessId")
            return
        }

        businessRepository.deactivate(businessId)
        logger.info("Successfully deactivated business: $businessId")
    }

    @Transactional(readOnly = true)
    fun validateBusinessExists(businessId: UUID): Business {
        return businessRepository.findById(businessId)
            ?: throw BusinessNotFoundException(businessId)
    }

    @Transactional(readOnly = true)
    fun validateBusinessActive(businessId: UUID): Business {
        val business = validateBusinessExists(businessId)
        if (!business.isActive) {
            throw InvalidBusinessOperationException("Business is not active: $businessId")
        }
        return business
    }

    private fun validateBusinessOwnership(businessId: UUID) {
        val userAttributes = identityFacade.extractUserAttributes()

        // Admins can access any business
        if (userAttributes.role == UserRole.ADMIN) {
            return
        }

        // Business owners can only access their own business
        if (userAttributes.role == UserRole.BUSINESS_OWNER) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not own business: $businessId")
            }
            return
        }

        throw UnauthorizedBusinessAccessException("Insufficient permissions for business ownership")
    }

    private fun validateBusinessAccess(businessId: UUID) {
        val userAttributes = identityFacade.extractUserAttributes()

        // Admins can access any business
        if (userAttributes.role == UserRole.ADMIN) {
            return
        }

        // Business owners and staff can access their business
        if (userAttributes.role == UserRole.BUSINESS_OWNER || userAttributes.role == UserRole.EMPLOYEE) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not have access to business: $businessId")
            }
            return
        }

        throw UnauthorizedBusinessAccessException("Insufficient permissions for business access")
    }
}