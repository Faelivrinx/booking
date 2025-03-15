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
    private var openingTime: LocalTime,
    private var closingTime: LocalTime,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) {
    private val domainEvents = mutableListOf<DomainEvent>()

    companion object {
        fun create(
            keycloakId: String,
            name: Name,
            email: Email,
            phoneNumber: PhoneNumber?,
            openingTime: LocalTime? = null,
            closingTime: LocalTime? = null
        ): Business {
            val effectiveOpeningTime = openingTime ?: LocalTime.of(9, 0)
            val effectiveClosingTime = closingTime ?: LocalTime.of(17, 0)

            if (effectiveOpeningTime.isAfter(effectiveClosingTime)) {
                throw BusinessDomainException("Opening time must be before closing time")
            }

            return Business(
                id = BusinessId.generate(),
                keycloakId = keycloakId,
                name = name,
                email = email,
                phone = phoneNumber,
                openingTime = effectiveOpeningTime,
                closingTime = effectiveClosingTime,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
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

    fun updateBusinessHours(openingTime: LocalTime, closingTime: LocalTime) {
        validateBusinessHours(openingTime, closingTime)

        this.openingTime = openingTime
        this.closingTime = closingTime
        this.updatedAt = LocalDateTime.now()
    }

    fun isWithinBusinessHours(dateTime: LocalDateTime): Boolean {
        val time = dateTime.toLocalTime()
        return !time.isBefore(openingTime) && !time.isAfter(closingTime)
    }

    fun getName(): Name = name

    fun getEmail(): Email = email

    fun getPhoneNumber(): PhoneNumber? = phone

    fun getOpeningTime(): LocalTime = openingTime

    fun getClosingTime(): LocalTime = closingTime

    fun getEvents(): List<DomainEvent> = domainEvents.toList()

    fun clearEvents() {
        domainEvents.clear()
    }
}