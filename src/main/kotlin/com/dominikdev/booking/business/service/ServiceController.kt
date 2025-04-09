package com.dominikdev.booking.business.service

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/services")
@PreAuthorize("hasRole('BUSINESS')")
class ServiceController(
    private val serviceManager: ServiceManager
) {
    /**
     * Get all services for a business
     */
    @GetMapping
    fun getServices(@PathVariable businessId: String): ResponseEntity<List<ServiceDTO>> {
        val services = serviceManager.getServices(UUID.fromString(businessId))
        return ResponseEntity.ok(services)
    }

    /**
     * Get a specific service by ID
     */
    @GetMapping("/{serviceId}")
    fun getServiceById(
        @PathVariable businessId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<ServiceDTO> {
        val service = serviceManager.getServiceById(
            UUID.fromString(businessId),
            UUID.fromString(serviceId)
        ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(service)
    }

    /**
     * Add a new service
     */
    @PostMapping
    fun addService(
        @PathVariable businessId: String,
        @RequestBody request: ServiceRequest
    ): ResponseEntity<ServiceDTO> {
        val newService = serviceManager.addService(
            businessId = UUID.fromString(businessId),
            name = request.name,
            durationMinutes = request.durationMinutes,
            description = request.description,
            price = request.price
        )

        return ResponseEntity(newService, HttpStatus.CREATED)
    }

    /**
     * Update an existing service
     */
    @PutMapping("/{serviceId}")
    fun updateService(
        @PathVariable businessId: String,
        @PathVariable serviceId: String,
        @RequestBody request: ServiceRequest
    ): ResponseEntity<ServiceDTO> {
        val updatedService = serviceManager.updateService(
            businessId = UUID.fromString(businessId),
            serviceId = UUID.fromString(serviceId),
            name = request.name,
            durationMinutes = request.durationMinutes,
            description = request.description,
            price = request.price
        )

        return ResponseEntity.ok(updatedService)
    }

    /**
     * Delete a service
     */
    @DeleteMapping("/{serviceId}")
    fun deleteService(
        @PathVariable businessId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Void> {
        val deleted = serviceManager.removeService(
            UUID.fromString(businessId),
            UUID.fromString(serviceId)
        )

        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Request object for creating/updating services
     */
    data class ServiceRequest(
        val name: String,
        val durationMinutes: Int,
        val description: String? = null,
        val price: BigDecimal? = null
    )
}