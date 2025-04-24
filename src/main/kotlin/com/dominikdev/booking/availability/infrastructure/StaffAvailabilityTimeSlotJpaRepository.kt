package com.dominikdev.booking.availability.infrastructure

import com.dominikdev.booking.availability.infrastructure.entity.StaffAvailabilityTimeSlotEntity
import com.dominikdev.booking.availability.infrastructure.entity.StaffAvailabilityTimeSlotId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.*

interface StaffAvailabilityTimeSlotJpaRepository :
    JpaRepository<StaffAvailabilityTimeSlotEntity, StaffAvailabilityTimeSlotId> {
    fun findByAvailabilityId(availabilityId: UUID): List<StaffAvailabilityTimeSlotEntity>

    @Modifying
    @Query("DELETE FROM StaffAvailabilityTimeSlotEntity t WHERE t.availabilityId = :availabilityId")
    fun deleteByAvailabilityId(availabilityId: UUID)
}