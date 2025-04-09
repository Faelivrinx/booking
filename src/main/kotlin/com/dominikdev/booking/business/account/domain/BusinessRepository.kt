package com.dominikdev.booking.business.account.domain

import com.dominikdev.booking.shared.values.Email

interface BusinessRepository {
    fun save(business: Business): Business
    fun findById(id: BusinessId): Business?
    fun findByKeycloakId(keycloakId: String): Business?
    fun findByEmail(email: Email): Business?
}