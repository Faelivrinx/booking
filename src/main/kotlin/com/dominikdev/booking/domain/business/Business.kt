package com.dominikdev.booking.domain.business

import com.dominikdev.booking.domain.event.DomainEvent
import com.dominikdev.booking.domain.exception.BusinessDomainException
import com.dominikdev.booking.domain.business.BusinessId
import com.dominikdev.booking.domain.shared.Email
import com.dominikdev.booking.domain.shared.Name
import com.dominikdev.booking.domain.shared.PhoneNumber
import java.time.LocalDateTime
import java.time.LocalTime

class Business private constructor(
    val id: BusinessId,
    val keycloakId: String,
    private var name: Name,
    private var email: Email,
    private var phone: PhoneNumber?,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) {
    private val domainEvents = mutableListOf<DomainEvent>()

    companion object {
        fun create(
            keycloakId: String,
            name: Name,
            email: Email,
            phoneNumber: PhoneNumber?
        ): Business {

            val business = Business(
                id = BusinessId.generate(),
                keycloakId = keycloakId,
                name = name,
                email = email,
                phone = phoneNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            business.registerEvent(
                BusinessCreatedEvent(
                    businessId = business.id,
                    keycloakId = business.keycloakId,
                    name = business.getName().value,
                    email = business.getEmail().value
                )
            )

            return business
        }
        fun reconstitute(
            id: BusinessId,
            keycloakId: String,
            name: String,
            email: String,
            phoneNumber: String?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): Business {
            return Business(
                id = id,
                keycloakId = keycloakId,
                name = Name.of(name),
                email = Email.of(email),
                phone = phoneNumber?.let { PhoneNumber.of(it) },
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    fun updateBusinessInfo(
        name: Name,
        email: Email,
        phoneNumber: PhoneNumber?,
    ) {
        this.name = name
        this.email = email
        this.phone = phoneNumber
        this.updatedAt = LocalDateTime.now()
    }

    fun getName(): Name = name

    fun getEmail(): Email = email

    fun getPhoneNumber(): PhoneNumber? = phone

    fun getEvents(): List<DomainEvent> = domainEvents.toList()

    fun clearEvents() {
        domainEvents.clear()
    }

    fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}