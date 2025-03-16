package com.dominikdev.booking.business.infrastructure.persistance

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BusinessJpaRepository : JpaRepository<BusinessEntity, UUID> {
    fun findByKeycloakId(keycloakId: String): BusinessEntity?
    fun findByEmail(email: String): BusinessEntity?
}