package com.dominikdev.booking.business.service

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "services")
data class ServiceEntity(
    @Id
    val id: UUID,

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int,

    @Column(length = 500)
    val description: String?,

    @Column(precision = 10, scale = 2)
    val price: BigDecimal?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)