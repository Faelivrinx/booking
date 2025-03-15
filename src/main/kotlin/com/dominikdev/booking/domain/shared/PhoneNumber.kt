package com.dominikdev.booking.domain.shared

import com.dominikdev.booking.domain.exception.BusinessDomainException

data class PhoneNumber private constructor(val value: String) {
    companion object {
        private val PHONE_REGEX = Regex("^\\+?[0-9\\s-()]{8,20}$")

        fun of(phoneNumber: String): PhoneNumber {
            if (phoneNumber.isBlank()) {
                throw BusinessDomainException("Phone number cannot be empty")
            }

            val normalized = phoneNumber.replace(Regex("[\\s-()]"), "")
            if (!PHONE_REGEX.matches(normalized)) {
                throw BusinessDomainException("Invalid phone number format: $phoneNumber")
            }

            return PhoneNumber(normalized)
        }

        fun ofNullable(phoneNumber: String?): PhoneNumber? {
            return if (phoneNumber.isNullOrBlank()) null else of(phoneNumber)
        }
    }

    override fun toString(): String = value
}