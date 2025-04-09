package com.dominikdev.booking.business.profile.infrastructure.persistance

import java.time.LocalDateTime
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "businesses")
class BusinessEntity(
    @Id
    val id: UUID,

    @Column(name = "keycloak_id", nullable = false, unique = true)
    val keycloakId: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "phone_number")
    val phoneNumber: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)