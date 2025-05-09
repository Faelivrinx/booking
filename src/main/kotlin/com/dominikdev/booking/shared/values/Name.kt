package com.dominikdev.booking.shared.values

import com.dominikdev.booking.business.identity.BusinessDomainException


data class Name private constructor(val value: String) {
    companion object {
        fun of(name: String): Name {
            if (name.isBlank()) {
                throw BusinessDomainException("Name cannot be empty")
            }
            if (name.length > 255) {
                throw BusinessDomainException("Name too long (max 255 characters)")
            }
            return Name(name.trim())
        }
    }

    override fun toString(): String = value
}