package com.dominikdev.booking.appointment.infrastructure.entity

import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "appointments")
class AppointmentEntity(
    @Id
    val id: UUID,

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(name = "client_id", nullable = false)
    val clientId: UUID,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: AppointmentStatus,

    @Column(length = 1000)
    val notes: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)