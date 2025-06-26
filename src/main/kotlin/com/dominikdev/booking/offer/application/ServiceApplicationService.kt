package com.dominikdev.booking.offer.application


import com.dominikdev.booking.identity.IdentityFacade
import com.dominikdev.booking.identity.domain.UserRole
import com.dominikdev.booking.offer.AddServiceRequest
import com.dominikdev.booking.offer.UpdateServiceRequest
import com.dominikdev.booking.offer.domain.*
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

open class ServiceApplicationService(
    private val serviceRepository: ServiceRepository,
    private val businessApplicationService: BusinessApplicationService,
    private val staffServiceAssignmentRepository: StaffServiceAssignmentRepository,
    private val identityFacade: IdentityFacade
) {

    private val logger = LoggerFactory.getLogger(ServiceApplicationService::class.java)

    @Transactional
    fun addService(businessId: UUID, request: AddServiceRequest): com.dominikdev.booking.offer.domain.Service {
        logger.info("Adding service '${request.name}' to business: $businessId")

        // Validate business ownership (only business owners can add services)
        validateBusinessOwnership(businessId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        // Check for duplicate service name within business
        if (serviceRepository.existsByBusinessIdAndName(businessId, request.name.trim())) {
            throw DuplicateServiceException(request.name)
        }

        val service = com.dominikdev.booking.offer.domain.Service(
            id = UUID.randomUUID(),
            businessId = businessId,
            name = request.name.trim(),
            description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            durationMinutes = request.durationMinutes,
            price = request.price,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val saved = serviceRepository.save(service)
        logger.info("Successfully added service: ${saved.id}")

        return saved
    }

    @Transactional
    fun updateService(businessId: UUID, serviceId: UUID, request: UpdateServiceRequest): com.dominikdev.booking.offer.domain.Service {
        logger.info("Updating service: $serviceId")

        // Validate business ownership
        validateBusinessOwnership(businessId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        val existingService = serviceRepository.findById(serviceId)
            ?: throw ServiceNotFoundException(serviceId)

        // Verify service belongs to the business
        if (existingService.businessId != businessId) {
            throw InvalidBusinessOperationException("Service does not belong to business: $businessId")
        }

        if (!existingService.isActive) {
            throw InvalidBusinessOperationException("Cannot update deactivated service")
        }

        // Check for duplicate name (excluding current service)
        val existingWithName = serviceRepository.findByBusinessIdAndName(businessId, request.name.trim())
        if (existingWithName != null && existingWithName.id != serviceId) {
            throw DuplicateServiceException(request.name)
        }

        val updatedService = existingService.update(
            name = request.name.trim(),
            description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            durationMinutes = request.durationMinutes,
            price = request.price
        )

        val saved = serviceRepository.save(updatedService)
        logger.info("Successfully updated service: $serviceId")

        return saved
    }

    @Transactional
    fun removeService(businessId: UUID, serviceId: UUID) {
        logger.info("Removing service: $serviceId")

        // Validate business ownership
        validateBusinessOwnership(businessId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        val service = serviceRepository.findById(serviceId)
            ?: throw ServiceNotFoundException(serviceId)

        // Verify service belongs to the business
        if (service.businessId != businessId) {
            throw InvalidBusinessOperationException("Service does not belong to business: $businessId")
        }

        // Remove all staff assignments for this service first
        staffServiceAssignmentRepository.deleteByServiceId(serviceId)

        // Hard delete the service
        serviceRepository.delete(serviceId)
        logger.info("Successfully removed service: $serviceId")
    }

    @Transactional(readOnly = true)
    fun getBusinessServices(businessId: UUID): List<com.dominikdev.booking.offer.domain.Service> {
        logger.debug("Retrieving services for business: $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        return serviceRepository.findByBusinessId(businessId)
    }

    @Transactional(readOnly = true)
    fun getActiveBusinessServices(businessId: UUID): List<com.dominikdev.booking.offer.domain.Service> {
        logger.debug("Retrieving active services for business: $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        return serviceRepository.findActiveByBusinessId(businessId)
    }

    @Transactional(readOnly = true)
    fun getService(businessId: UUID, serviceId: UUID): com.dominikdev.booking.offer.domain.Service? {
        logger.debug("Retrieving service: $serviceId")

        // Validate business access
        validateBusinessAccess(businessId)

        val service = serviceRepository.findById(serviceId)

        // Verify service belongs to the business if found
        if (service != null && service.businessId != businessId) {
            return null
        }

        return service
    }

    @Transactional(readOnly = true)
    fun validateServiceExists(serviceId: UUID): com.dominikdev.booking.offer.domain.Service {
        return serviceRepository.findById(serviceId)
            ?: throw ServiceNotFoundException(serviceId)
    }

    @Transactional(readOnly = true)
    fun validateServiceActive(serviceId: UUID): com.dominikdev.booking.offer.domain.Service {
        val service = validateServiceExists(serviceId)
        if (!service.isActive) {
            throw InvalidBusinessOperationException("Service is not active: $serviceId")
        }
        return service
    }

    // Methods for other contexts (no security validation needed)
    @Transactional(readOnly = true)
    fun getServiceDuration(serviceId: UUID): Int? {
        return serviceRepository.findById(serviceId)?.durationMinutes
    }

    @Transactional(readOnly = true)
    fun getServicePrice(serviceId: UUID): java.math.BigDecimal? {
        return serviceRepository.findById(serviceId)?.price
    }

    private fun validateBusinessOwnership(businessId: UUID) {
        val userAttributes = identityFacade.extractUserAttributes()

        // Admins can modify any business
        if (userAttributes.role == UserRole.ADMIN) {
            return
        }

        // Only business owners can modify services
        if (userAttributes.role == UserRole.BUSINESS_OWNER) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not own business: $businessId")
            }
            return
        }

        throw UnauthorizedBusinessAccessException("Only business owners can modify services")
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