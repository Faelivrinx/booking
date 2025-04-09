package com.dominikdev.booking.business.account.infrastructure.adapter.`in`

import com.dominikdev.booking.business.account.application.command.CreateBusinessCommand
import com.dominikdev.booking.business.account.application.command.UpdateBusinessCommand
import com.dominikdev.booking.business.account.application.dto.BusinessDTO
import com.dominikdev.booking.business.account.BusinessAccountFacade
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
class BusinessController(private val businessAccountFacade: BusinessAccountFacade) {

    @PostMapping
    fun createBusiness(@RequestBody request: CreateBusinessRequest): ResponseEntity<BusinessDTO> {
        val command = CreateBusinessCommand(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber,
            initialPassword = request.password
        )

        val business = businessAccountFacade.createBusiness(command)
        return ResponseEntity(business, HttpStatus.CREATED)
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

        val business = businessAccountFacade.updateBusiness(UUID.fromString(businessId), command)
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