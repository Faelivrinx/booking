package com.dominikdev.booking.appointment.infrastructure.readmodel

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
/**
 * Read model entity for staff daily schedules
 * Combines availability and booked appointments in a single view
 */
@Entity
@Table(
    name = "staff_daily_schedules",
    indexes = [
        Index(name = "idx_sds_business_date", columnList = "business_id, date"),
        Index(name = "idx_sds_staff_date", columnList = "staff_id, date")
    ]
)
class StaffDailySchedule(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "is_available", nullable = false)
    val isAvailable: Boolean,

    @Column(name = "appointment_id")
    val appointmentId: UUID? = null,

    @Column(name = "client_name", length = 100)
    val clientName: String? = null,

    @Column(name = "service_name", length = 100)
    val serviceName: String? = null
)