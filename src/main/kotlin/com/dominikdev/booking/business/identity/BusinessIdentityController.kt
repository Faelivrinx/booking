package com.dominikdev.booking.business.identity

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/identity/businesses")
class BusinessIdentityController(private val businessAccountFacade: BusinessIdentityFacade) {

    @PostMapping
    fun createBusiness(@RequestBody request: CreateBusinessRequest): ResponseEntity<BusinessIdentityDTO> {
        val command = CreateBusinessIdentityCommand(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber,
            initialPassword = request.password,
            businessId = request.businessId
        )

        val business = businessAccountFacade.createBusinessIdentity(command)
        return ResponseEntity(business, HttpStatus.CREATED)
    }

    @PutMapping("/{businessId}")
    fun updateBusiness(
        @PathVariable businessId: String,
        @RequestBody request: UpdateBusinessRequest
    ): ResponseEntity<BusinessIdentityDTO> {
        val command = UpdateBusinessCommand(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber
        )

        val business = businessAccountFacade.updateBusiness(UUID.fromString(businessId), command)
        return ResponseEntity.ok(business)
    }

    data class CreateBusinessRequest(
        val name: String,
        val email: String,
        val phoneNumber: String?,
        val password: String,
        val businessId: UUID
    )

    data class UpdateBusinessRequest(
        val name: String,
        val email: String,
        val phoneNumber: String?
    )
}