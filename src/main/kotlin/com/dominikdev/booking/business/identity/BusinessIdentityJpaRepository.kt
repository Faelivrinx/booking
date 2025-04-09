package com.dominikdev.booking.business.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BusinessIdentityJpaRepository : JpaRepository<BusinessIdentity, UUID> {
    fun findByKeycloakId(keycloakId: String): BusinessIdentity?
    fun findByEmail(email: String): BusinessIdentity?
}