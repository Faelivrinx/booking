package com.dominikdev.booking.clients.identity

import com.dominikdev.booking.shared.event.DomainEvent
import com.dominikdev.booking.shared.values.Email
import com.dominikdev.booking.shared.values.PhoneNumber
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "clients_identity")
class ClientIdentity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "keycloak_id", unique = true)
    var keycloakId: String? = null,

    @Column(nullable = false, unique = true)
    private var email: String,

    @Column(name = "phone_number", nullable = false)
    private var phoneNumber: String,

    @Column(name = "first_name", nullable = false)
    private var firstName: String,

    @Column(name = "last_name", nullable = false)
    private var lastName: String,

    @Column(nullable = false)
    private var verified: Boolean = false,

    @Column(name = "verification_code")
    private var verificationCode: String? = null,

    @Column(name = "verification_code_expiry")
    private var verificationCodeExpiry: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Transient
    private var domainEvents = mutableListOf<DomainEvent>()

    companion object {
        fun register(
            email: Email,
            phoneNumber: PhoneNumber,
            firstName: String,
            lastName: String,
            verificationCode: String
        ): ClientIdentity {
            val clientIdentity = ClientIdentity(
                email = email.value,
                phoneNumber = phoneNumber.value,
                firstName = firstName,
                lastName = lastName,
                verificationCode = verificationCode,
                verificationCodeExpiry = LocalDateTime.now().plusHours(24)
            )

            clientIdentity.registerEvent(
                ClientRegisteredEvent(
                    clientId = clientIdentity.id,
                    email = clientIdentity.email,
                    phoneNumber = clientIdentity.phoneNumber
                )
            )

            return clientIdentity
        }
    }

    fun verify(code: String, keycloakId: String): Boolean {
        if (verified) {
            throw ClientDomainException("Client already verified")
        }

        if (verificationCode != code) {
            return false
        }

        if (verificationCodeExpiry?.isBefore(LocalDateTime.now()) == true) {
            throw ClientDomainException("Verification code has expired")
        }

        this.verified = true
        this.keycloakId = keycloakId
        this.verificationCode = null
        this.verificationCodeExpiry = null
        this.updatedAt = LocalDateTime.now()

        registerEvent(
            ClientActivatedEvent(
                clientId = this.id,
                email = this.email,
                keycloakId = keycloakId
            )
        )

        return true
    }

    fun regenerateVerificationCode(newCode: String): String {
        if (verified) {
            throw ClientDomainException("Cannot regenerate verification code for verified client")
        }

        this.verificationCode = newCode
        this.verificationCodeExpiry = LocalDateTime.now().plusHours(24)
        this.updatedAt = LocalDateTime.now()

        registerEvent(
            ClientVerificationCodeRegeneratedEvent(
                clientId = this.id,
                phoneNumber = this.phoneNumber
            )
        )

        return newCode
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: PhoneNumber
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.phoneNumber = phoneNumber.value
        this.updatedAt = LocalDateTime.now()
    }

    fun getEmail(): String = email
    fun getPhoneNumber(): String = phoneNumber
    fun getFirstName(): String = firstName
    fun getLastName(): String = lastName
    fun isVerified(): Boolean = verified
    fun getVerificationCode(): String? = verificationCode
    fun getVerificationCodeExpiry(): LocalDateTime? = verificationCodeExpiry

    fun getFullName(): String = "$firstName $lastName"

    fun getEvents(): List<DomainEvent> = domainEvents.toList()
    fun clearEvents() {
        domainEvents.clear()
    }
    private fun registerEvent(event: DomainEvent) {
        if (domainEvents == null) {
            domainEvents = mutableListOf()
        }
        domainEvents.add(event)
    }
}