package com.dominikdev.booking.business.service

import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class ServiceManager(
    private val serviceRepository: ServiceRepository,
    private val eventPublisher: DomainEventPublisher
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Gets all services for a business
     */
    @Transactional(readOnly = true)
    fun getServices(businessId: UUID): List<ServiceDTO> {
        return serviceRepository.findAllByBusinessId(businessId)
            .map { mapToDTO(it) }
    }

    /**
     * Gets a specific service by ID
     */
    @Transactional(readOnly = true)
    fun getServiceById(businessId: UUID, serviceId: UUID): ServiceDTO? {
        return serviceRepository.findById(serviceId)
            .filter { it.businessId == businessId } // Ensure service belongs to requested business
            .map { mapToDTO(it) }
            .orElse(null)
    }

    /**
     * Gets a specific service by name
     */
    @Transactional(readOnly = true)
    fun getServiceByName(businessId: UUID, name: String): ServiceDTO? {
        return serviceRepository.findByBusinessIdAndName(businessId, name)
            ?.let { mapToDTO(it) }
    }

    /**
     * Adds a new service for a business
     */
    @Transactional
    fun addService(
        businessId: UUID,
        name: String,
        durationMinutes: Int,
        description: String?,
        price: BigDecimal?
    ): ServiceDTO {
        // Validate inputs
        validateName(name)
        validateDuration(durationMinutes)
        validateDescription(description)

        // Check if service with same name already exists
        if (serviceRepository.existsByBusinessIdAndName(businessId, name.trim())) {
            throw ServicesException("Service with name '$name' already exists")
        }

        // Create new service entity
        val serviceId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val serviceEntity = ServiceEntity(
            id = serviceId,
            businessId = businessId,
            name = name.trim(),
            durationMinutes = durationMinutes,
            description = description?.trim(),
            price = price,
            createdAt = now,
            updatedAt = now
        )

        // Save the service entity
        val savedService = serviceRepository.save(serviceEntity)

        // Publish event
        eventPublisher.publish(
            ServiceCreatedEvent(
                serviceId = serviceId,
                businessId = businessId,
                serviceName = name.trim()
            )
        )

        return mapToDTO(savedService)
    }

    /**
     * Updates an existing service
     */
    @Transactional
    fun updateService(
        businessId: UUID,
        serviceId: UUID,
        name: String,
        durationMinutes: Int,
        description: String?,
        price: BigDecimal?
    ): ServiceDTO {
        // Validate inputs
        validateName(name)
        validateDuration(durationMinutes)
        validateDescription(description)

        // Find the service to update
        val existingService = serviceRepository.findById(serviceId)
            .orElseThrow { ServicesException("Service with ID $serviceId not found") }

        // Check service belongs to the business
        if (existingService.businessId != businessId) {
            throw ServicesException("Service does not belong to this business")
        }

        // Check for name conflicts if name is changing
        if (existingService.name != name.trim()) {
            if (serviceRepository.existsByBusinessIdAndName(businessId, name.trim())) {
                throw ServicesException("Another service with name '$name' already exists")
            }
        }

        // Create updated service entity
        val updatedService = ServiceEntity(
            id = existingService.id,
            businessId = existingService.businessId,
            name = name.trim(),
            durationMinutes = durationMinutes,
            description = description?.trim(),
            price = price,
            createdAt = existingService.createdAt,
            updatedAt = LocalDateTime.now()
        )

        // Save the updated entity
        val savedService = serviceRepository.save(updatedService)

        // Publish event
        eventPublisher.publish(
            ServiceUpdatedEvent(
                serviceId = serviceId,
                businessId = businessId,
                serviceName = name.trim()
            )
        )

        return mapToDTO(savedService)
    }

    /**
     * Removes a service
     */
    @Transactional
    fun removeService(businessId: UUID, serviceId: UUID): Boolean {
        // Find the service
        val service = serviceRepository.findById(serviceId).orElse(null) ?: return false

        // Check service belongs to the business
        if (service.businessId != businessId) {
            return false
        }

        // Save the service name before deletion for event
        val serviceName = service.name

        // Delete the service
        serviceRepository.deleteById(serviceId)

        // Publish event
        eventPublisher.publish(
            ServiceDeletedEvent(
                serviceId = serviceId,
                businessId = businessId,
                serviceName = serviceName
            )
        )

        logger.info { "Deleted service: $serviceId for business: $businessId" }
        return true
    }

    /**
     * Maps a ServiceEntity to ServiceDTO
     */
    private fun mapToDTO(entity: ServiceEntity): ServiceDTO {
        return ServiceDTO(
            id = entity.id,
            businessId = entity.businessId,
            name = entity.name,
            durationMinutes = entity.durationMinutes,
            description = entity.description,
            price = entity.price,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    // Validation methods
    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ServicesException("Service name cannot be empty")
        }
        if (name.length > 100) {
            throw ServicesException("Service name is too long (max 100 characters)")
        }
    }

    private fun validateDuration(durationMinutes: Int) {
        if (durationMinutes <= 0) {
            throw ServicesException("Service duration must be positive")
        }
        if (durationMinutes > 1440) { // 24 hours in minutes
            throw ServicesException("Service duration cannot exceed 24 hours")
        }
    }

    private fun validateDescription(description: String?) {
        if (description != null && description.length > 500) {
            throw ServicesException("Service description is too long (max 500 characters)")
        }
    }
}