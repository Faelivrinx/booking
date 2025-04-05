package com.dominikdev.booking.clients

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/clients")
@CrossOrigin(value = ["*"])
class ClientController(private val clientService: ClientService) {

    @PostMapping("/register")
    fun registerClient(@RequestBody request: RegisterClientRequest): ResponseEntity<RegistrationResponse> {
        val registrationResponse = clientService.registerClient(request)
        return ResponseEntity(registrationResponse, HttpStatus.CREATED)
    }

    @PostMapping("/activate")
    fun activateClient(@RequestBody request: ActivateClientRequest): ResponseEntity<ClientResponse> {
        val client = clientService.activateClient(request)
        return ResponseEntity(client, HttpStatus.OK)
    }

    @PostMapping("/resend-code")
    fun resendVerificationCode(@RequestBody request: ResendVerificationCodeRequest): ResponseEntity<Map<String, Boolean>> {
        val result = clientService.regenerateVerificationCode(request)
        return ResponseEntity(mapOf("sent" to result), HttpStatus.OK)
    }

    @GetMapping("/{clientId}")
    fun getClientById(@PathVariable clientId: String): ResponseEntity<ClientResponse> {
        val client = clientService.getClientById(UUID.fromString(clientId))
        return ResponseEntity.ok(client)
    }

    @PutMapping("/{clientId}")
    fun updateClientProfile(
        @PathVariable clientId: String,
        @RequestBody request: UpdateClientProfileRequest
    ): ResponseEntity<ClientResponse> {
        val client = clientService.updateClientProfile(UUID.fromString(clientId), request)
        return ResponseEntity.ok(client)
    }
}

// Request and Response classes
data class RegisterClientRequest(
    val email: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String
)

data class RegistrationResponse(
    val id: UUID,
    val email: String,
    val phoneNumber: String,
    val verificationCodeSent: Boolean,
    // Just for testing - would be removed in production
    val verificationCode: String? = null
)

data class ActivateClientRequest(
    val email: String,
    val verificationCode: String,
    val password: String
)

data class ResendVerificationCodeRequest(
    val email: String
)

data class UpdateClientProfileRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String
)

data class ClientResponse(
    val id: UUID,
    val email: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val verified: Boolean,
    val createdAt: LocalDateTime
)
