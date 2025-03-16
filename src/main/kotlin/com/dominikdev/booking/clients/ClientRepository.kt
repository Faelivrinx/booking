package com.dominikdev.booking.clients

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClientRepository : JpaRepository<Client, UUID> {
    fun findByKeycloakId(keycloakId: String): Client?
    fun findByEmail(email: String): Client?
    fun findByPhoneNumber(phoneNumber: String): Client?
}