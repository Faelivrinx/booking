package com.dominikdev.booking.infrastructure.api

import com.dominikdev.booking.application.command.CreateBusinessCommand
import com.dominikdev.booking.application.dto.BusinessDTO
import com.dominikdev.booking.application.service.BusinessApplicationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/businesses")
class BusinessController(private val businessApplicationService: BusinessApplicationService) {

    @PostMapping
    fun createBusiness(@RequestBody request: CreateBusinessRequest): ResponseEntity<BusinessDTO> {
        val command = CreateBusinessCommand(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber,
            openingTime = request.openingTime,
            closingTime = request.closingTime,
            initialPassword = request.password
        )

        val business = businessApplicationService.createBusiness(command)
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
        // This would require adding a method to the BusinessApplicationService
        // For now, let's assume it exists
        // val business = businessApplicationService.getBusinessById(businessId)
        // return ResponseEntity.ok(business)

        // Temporary placeholder response
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    data class CreateBusinessRequest(
        val name: String,
        val email: String,
        val phoneNumber: String?,
        val openingTime: java.time.LocalTime?,
        val closingTime: java.time.LocalTime?,
        val password: String
    )
}