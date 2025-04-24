package com.dominikdev.booking.availability.infrastructure

import com.dominikdev.booking.availability.infrastructure.entity.StaffAvailabilityEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.*

interface StaffAvailabilityJpaRepository : JpaRepository<StaffAvailabilityEntity, UUID> {
    fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): StaffAvailabilityEntity?
    fun findByStaffIdAndDateBetween(staffId: UUID, startDate: LocalDate, endDate: LocalDate): List<StaffAvailabilityEntity>
    fun findByBusinessIdAndDate(businessId: UUID, date: LocalDate): List<StaffAvailabilityEntity>
    fun existsByStaffIdAndDate(staffId: UUID, date: LocalDate): Boolean
}