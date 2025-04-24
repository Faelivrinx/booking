package com.dominikdev.booking.availability.infrastructure.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "staff_daily_availability")
data class StaffAvailabilityEntity(
    @Id
    val id: UUID,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)