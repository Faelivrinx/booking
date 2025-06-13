package com.dominikdev.booking.identity.infrastructure

import com.dominikdev.booking.identity.domain.UserRole
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_profiles")
data class UserProfileEntity(
    @Id
    val id: UUID,

    @Column(name = "keycloak_id", unique = true, nullable = false)
    val keycloakId: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "phone_number")
    val phoneNumber: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,

    @Column(name = "business_id")
    val businessId: UUID?,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)