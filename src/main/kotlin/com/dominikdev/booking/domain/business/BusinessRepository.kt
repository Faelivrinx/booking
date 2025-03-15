package com.dominikdev.booking.domain.business

import com.dominikdev.booking.domain.shared.Email

interface BusinessRepository {
    fun save(business: Business): Business
    fun findById(id: BusinessId): Business?
    fun findByKeycloakId(keycloakId: String): Business?
    fun findByEmail(email: Email): Business?
}