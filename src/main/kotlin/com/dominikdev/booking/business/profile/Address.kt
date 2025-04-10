package com.dominikdev.booking.business.profile

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class Address(
    @Column(nullable = false)
    val street: String,

    @Column(nullable = false)
    val city: String,

    @Column
    val state: String?,

    @Column(name = "postal_code", nullable = false)
    val postalCode: String,

) {
    override fun toString(): String {
        val stateStr = state?.let { ", $it" } ?: ""
        return "$street, $city$stateStr, $postalCode"
    }

    companion object {
        fun createEmpty(): Address {
            return Address(
                street = "",
                city = "",
                state = null,
                postalCode = "",
            )
        }
    }
}