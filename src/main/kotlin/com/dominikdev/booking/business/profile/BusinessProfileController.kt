package com.dominikdev.booking.business.profile

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/businesses")
class BusinessProfileController(
    private val businessProfileService: BusinessProfileService
) {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createBusiness(@RequestBody request: CreateBusinessRequest): ResponseEntity<BusinessProfileDTO> {
        val command = CreateBusinessProfileCommand(
            // Business details
            name = request.name,
            description = request.description,
            street = request.street,
            city = request.city,
            state = request.state,
            postalCode = request.postalCode,
            // Owner details
            ownerName = request.ownerName,
            ownerEmail = request.ownerEmail,
            ownerPhone = request.ownerPhone,
            ownerPassword = request.ownerPassword
        )

        val business = businessProfileService.createBusinessWithOwner(command)
        return ResponseEntity(business, HttpStatus.CREATED)
    }

    @GetMapping("/{businessId}")
    fun getBusinessById(@PathVariable businessId: String): ResponseEntity<BusinessProfileDTO> {
        val business = businessProfileService.getBusinessById(UUID.fromString(businessId))
        return ResponseEntity.ok(business)
    }

    @PutMapping("/{businessId}")
    fun updateBusiness(
        @PathVariable businessId: String,
        @RequestBody request: UpdateBusinessRequest
    ): ResponseEntity<BusinessProfileDTO> {
        val command = UpdateBusinessProfileCommand(
            name = request.name,
            description = request.description,
            street = request.street,
            city = request.city,
            state = request.state,
            postalCode = request.postalCode,
        )

        val business = businessProfileService.updateBusiness(
            UUID.fromString(businessId),
            command
        )

        return ResponseEntity.ok(business)
    }

    // Other endpoints...

    data class CreateBusinessRequest(
        // Business details
        val name: String,
        val description: String?,
        val street: String,
        val city: String,
        val state: String?,
        val postalCode: String,
        // Owner details
        val ownerName: String,
        val ownerEmail: String,
        val ownerPhone: String?,
        val ownerPassword: String
    )

    data class UpdateBusinessRequest(
        val name: String,
        val description: String?,
        val street: String,
        val city: String,
        val state: String?,
        val postalCode: String,
    )
}