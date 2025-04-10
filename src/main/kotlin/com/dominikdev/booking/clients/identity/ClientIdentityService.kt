package com.dominikdev.booking.clients.identity

import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import com.dominikdev.booking.shared.infrastructure.identity.IdentityManagementService
import com.dominikdev.booking.shared.values.Email
import com.dominikdev.booking.shared.values.PhoneNumber
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.random.Random

@Service
class ClientIdentityService(
    private val clientIdentityRepository: ClientIdentityRepository,
    private val smsService: SmsService,
    private val keycloakAdapter: IdentityManagementService,
    private val eventPublisher: DomainEventPublisher
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun registerClient(request: RegisterClientRequest): RegistrationResponse {
        val email = Email.of(request.email)
        val phoneNumber = PhoneNumber.of(request.phoneNumber)

        clientIdentityRepository.findByEmail(email.value)?.let {
            if (it.isVerified()) {
                throw ClientDomainException("Client with email ${request.email} already exists")
            } else {
                val newCode = generateVerificationCode()
                it.regenerateVerificationCode(newCode)
                val savedClient = clientIdentityRepository.save(it)

                publishEvents(it)

                sendVerificationSms(it.getPhoneNumber(), newCode)

                return RegistrationResponse(
                    id = savedClient.id,
                    email = savedClient.getEmail(),
                    phoneNumber = savedClient.getPhoneNumber(),
                    verificationCodeSent = true,
                    verificationCode = if (shouldExposeVerificationCode()) newCode else null
                )
            }
        }

        clientIdentityRepository.findByPhoneNumber(phoneNumber.value)?.let {
            if (it.isVerified()) {
                throw ClientDomainException("Phone number ${request.phoneNumber} already in use")
            }
        }

        val verificationCode = generateVerificationCode()

        val clientIdentity = ClientIdentity.register(
            email = email,
            phoneNumber = phoneNumber,
            firstName = request.firstName,
            lastName = request.lastName,
            verificationCode = verificationCode
        )

        val savedClient = clientIdentityRepository.save(clientIdentity)

        publishEvents(clientIdentity)

        sendVerificationSms(phoneNumber.value, verificationCode)

        return RegistrationResponse(
            id = savedClient.id,
            email = savedClient.getEmail(),
            phoneNumber = savedClient.getPhoneNumber(),
            verificationCodeSent = true,
            verificationCode = if (shouldExposeVerificationCode()) verificationCode else null
        )
    }

    @Transactional
    fun activateClient(request: ActivateClientRequest): ClientResponse {
        val email = Email.of(request.email)

        val client = clientIdentityRepository.findByEmail(email.value)
            ?: throw ClientDomainException("No registration found for email ${request.email}")

        if (client.isVerified()) {
            throw ClientDomainException("Client is already verified")
        }

        try {
            val keycloakId = keycloakAdapter.createClientUser(
                email = email.value,
                name = "${client.getFirstName()} ${client.getLastName()}",
                phone = client.getPhoneNumber(),
                password = request.password
            )

            val verified = client.verify(request.verificationCode, keycloakId)

            if (!verified) {
                try {
                    keycloakAdapter.deleteUser(keycloakId)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to delete Keycloak user after verification failure: $keycloakId" }
                }
                throw ClientDomainException("Invalid verification code")
            }

            val savedClient = clientIdentityRepository.save(client)

            publishEvents(client)

            return mapToResponse(savedClient)
        } catch (e: Exception) {
            logger.error(e) { "Failed to activate client: ${e.message}" }
            if (e is ClientDomainException) {
                throw e
            }
            throw ClientDomainException("Failed to activate client account", e)
        }
    }

    @Transactional
    fun regenerateVerificationCode(request: ResendVerificationCodeRequest): Boolean {
        val email = Email.of(request.email)

        val client = clientIdentityRepository.findByEmail(email.value)
            ?: throw ClientDomainException("No registration found for email ${request.email}")

        if (client.isVerified()) {
            throw ClientDomainException("Client is already verified")
        }

        val newCode = generateVerificationCode()
        client.regenerateVerificationCode(newCode)

        clientIdentityRepository.save(client)

        publishEvents(client)

        return sendVerificationSms(client.getPhoneNumber(), newCode)
    }

    @Transactional
    fun updateClientProfile(clientId: UUID, request: UpdateClientProfileRequest): ClientResponse {
        val client = clientIdentityRepository.findById(clientId)
            .orElseThrow { ClientDomainException("Client not found for id: $clientId") }

        if (!client.isVerified()) {
            throw ClientDomainException("Client is not verified yet")
        }

        val phoneNumber = PhoneNumber.of(request.phoneNumber)

        client.updateProfile(
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = phoneNumber
        )

        val updatedClient = clientIdentityRepository.save(client)

        return mapToResponse(updatedClient)
    }

    @Transactional(readOnly = true)
    fun getClientById(clientId: UUID): ClientResponse {
        val client = clientIdentityRepository.findById(clientId)
            .orElseThrow { ClientDomainException("Client not found for id: $clientId") }

        return mapToResponse(client)
    }

    @Transactional(readOnly = true)
    fun getClientByEmail(email: String): ClientResponse {
        val client = clientIdentityRepository.findByEmail(email)
            ?: throw ClientDomainException("Client not found for email: $email")

        return mapToResponse(client)
    }

    @Transactional(readOnly = true)
    fun getClientByKeycloakId(keycloakId: String): ClientResponse {
        val client = clientIdentityRepository.findByKeycloakId(keycloakId)
            ?: throw ClientDomainException("Client not found for keycloakId: $keycloakId")

        return mapToResponse(client)
    }

    private fun mapToResponse(clientIdentity: ClientIdentity): ClientResponse {
        return ClientResponse(
            id = clientIdentity.id,
            email = clientIdentity.getEmail(),
            phoneNumber = clientIdentity.getPhoneNumber(),
            firstName = clientIdentity.getFirstName(),
            lastName = clientIdentity.getLastName(),
            verified = clientIdentity.isVerified(),
            createdAt = clientIdentity.createdAt
        )
    }

    private fun generateVerificationCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun shouldExposeVerificationCode(): Boolean {
        // In production, we would never expose the verification code
        // This is just for testing
        return true
    }

    private fun sendVerificationSms(phoneNumber: String, code: String): Boolean {
        return try {
            smsService.sendVerificationSms(phoneNumber, code)
        } catch (e: Exception) {
            logger.error(e) { "Failed to send verification SMS to $phoneNumber: ${e.message}" }
            false
        }
    }

    private fun publishEvents(clientIdentity: ClientIdentity) {
        val events = clientIdentity.getEvents()
        if (events.isNotEmpty()) {
            eventPublisher.publishAll(events)
            clientIdentity.clearEvents()
        }
    }
}