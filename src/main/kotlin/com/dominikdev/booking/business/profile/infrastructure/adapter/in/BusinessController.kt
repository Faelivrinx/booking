package com.dominikdev.booking.business.profile.infrastructure.adapter.`in`

import com.dominikdev.booking.business.profile.application.command.CreateBusinessCommand
import com.dominikdev.booking.business.profile.application.command.UpdateBusinessCommand
import com.dominikdev.booking.business.profile.application.dto.BusinessDTO
import com.dominikdev.booking.business.profile.BusinessFacade
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
class BusinessController(private val businessFacade: BusinessFacade) {

    @PostMapping
    fun createBusiness(@RequestBody request: CreateBusinessRequest): ResponseEntity<BusinessDTO> {
        val command = CreateBusinessCommand(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber,
            initialPassword = request.password
        )

        val business = businessFacade.createBusiness(command)
        return ResponseEntity(business, HttpStatus.CREATED)
    }


//    @GetMapping("/me")
//    fun getCurrentBusiness(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<BusinessDTO> {
//        val keycloakId = jwt.subject
//        val business = businessApplicationService.getBusinessByKeycloakId(keycloakId)
//        return ResponseEntity.ok(business)
//    }

    @GetMapping("/{businessId}")
    fun getBusinessById(@PathVariable businessId: String): ResponseEntity<BusinessDTO> {
        val business = businessFacade.getBusinessById(UUID.fromString(businessId))
        return ResponseEntity.ok(business)
    }

    @PutMapping("/{businessId}")
    fun updateBusiness(
        @PathVariable businessId: String,
        @RequestBody request: UpdateBusinessRequest
    ): ResponseEntity<BusinessDTO> {
        val command = UpdateBusinessCommand(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber
        )

        val business = businessFacade.updateBusiness(UUID.fromString(businessId), command)
        return ResponseEntity.ok(business)
    }

    data class CreateBusinessRequest(
        val name: String,
        val email: String,
        val phoneNumber: String?,
        val password: String
    )

    data class UpdateBusinessRequest(
        val name: String,
        val email: String,
        val phoneNumber: String?
    )
}