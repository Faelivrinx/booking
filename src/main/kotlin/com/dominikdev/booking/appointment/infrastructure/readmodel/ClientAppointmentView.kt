package com.dominikdev.booking.appointment.infrastructure.readmodel

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Read model entity for client appointments
 * Provides a denormalized view of appointment data optimized for client views
 */
@Entity
@Table(
    name = "client_appointments",
    indexes = [
        Index(name = "idx_ca_client_date", columnList = "client_id, date"),
        Index(name = "idx_ca_business", columnList = "business_id")
    ]
)
class ClientAppointmentView(
    @Id
    val id: UUID,

    @Column(name = "client_id", nullable = false)
    val clientId: UUID,

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(name = "business_name", nullable = false, length = 100)
    val businessName: String,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "service_name", nullable = false, length = 100)
    val serviceName: String,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(name = "staff_name", nullable = false, length = 100)
    val staffName: String,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(nullable = false, length = 20)
    val status: String,

    @Column(precision = 10, scale = 2)
    val price: BigDecimal? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: java.time.LocalDateTime
)