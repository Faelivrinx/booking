package com.dominikdev.booking.offer.domain

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(state.isNotBlank()) { "State cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
    }

    fun getFullAddress(): String = "$street, $city, $state $postalCode"
}