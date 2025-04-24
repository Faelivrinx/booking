package com.dominikdev.booking.availability.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalTime
import java.util.*

@Entity
@Table(name = "staff_availability_time_slots")
@IdClass(StaffAvailabilityTimeSlotId::class)
data class StaffAvailabilityTimeSlotEntity(
    @Id
    @Column(name = "availability_id")
    val availabilityId: UUID,

    @Id
    @Column(name = "start_time")
    val startTime: LocalTime,

    @Id
    @Column(name = "end_time")
    val endTime: LocalTime
)

data class StaffAvailabilityTimeSlotId(
    val availabilityId: UUID = UUID.randomUUID(),
    val startTime: LocalTime = LocalTime.MIN,
    val endTime: LocalTime = LocalTime.MAX
) : java.io.Serializable
