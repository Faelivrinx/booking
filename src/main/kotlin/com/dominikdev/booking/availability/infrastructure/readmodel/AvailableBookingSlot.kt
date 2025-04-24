package com.dominikdev.booking.availability.infrastructure.readmodel

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(
    name = "available_booking_slots",
    indexes = [
        Index(name = "idx_abs_business_date", columnList = "business_id, date"),
        Index(name = "idx_abs_service_date", columnList = "service_id, date"),
        Index(name = "idx_abs_staff_date", columnList = "staff_id, date")
    ]
)
class AvailableBookingSlot(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "service_duration_minutes", nullable = false)
    val serviceDurationMinutes: Int,

    @Column(name = "service_name", nullable = false, length = 100)
    val serviceName: String,

    @Column(name = "staff_name", nullable = false, length = 100)
    val staffName: String,

    @Column(name = "service_price", precision = 10, scale = 2)
    val servicePrice: BigDecimal? = null
)