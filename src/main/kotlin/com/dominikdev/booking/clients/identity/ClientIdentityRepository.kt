package com.dominikdev.booking.clients.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClientIdentityRepository : JpaRepository<ClientIdentity, UUID> {
    fun findByKeycloakId(keycloakId: String): ClientIdentity?
    fun findByEmail(email: String): ClientIdentity?
    fun findByPhoneNumber(phoneNumber: String): ClientIdentity?
}