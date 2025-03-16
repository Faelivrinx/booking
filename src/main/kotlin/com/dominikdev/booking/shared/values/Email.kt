package com.dominikdev.booking.shared.values

import com.dominikdev.booking.business.domain.BusinessDomainException

data class Email private constructor(val value: String) {
    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

        fun of(email: String): Email {
            if (email.isBlank()) {
                throw BusinessDomainException("Email cannot be empty")
            }
            if (!EMAIL_REGEX.matches(email)) {
                throw BusinessDomainException("Invalid email format: $email")
            }
            return Email(email.trim().lowercase())
        }
    }

    override fun toString(): String = value
}