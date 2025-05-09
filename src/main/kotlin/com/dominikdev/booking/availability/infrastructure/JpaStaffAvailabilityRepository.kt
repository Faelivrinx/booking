package com.dominikdev.booking.availability.infrastructure

import com.dominikdev.booking.availability.domain.model.StaffDailyAvailability
import com.dominikdev.booking.availability.domain.model.TimeSlot
import com.dominikdev.booking.availability.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.availability.infrastructure.entity.StaffAvailabilityEntity
import com.dominikdev.booking.availability.infrastructure.entity.StaffAvailabilityTimeSlotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Repository
class JpaStaffAvailabilityRepository(
    private val availabilityJpaRepository: StaffAvailabilityJpaRepository,
    private val timeSlotJpaRepository: StaffAvailabilityTimeSlotJpaRepository
) : StaffAvailabilityRepository {

    override fun save(availability: StaffDailyAvailability): StaffDailyAvailability {
        // Convert domain to entity
        var entity = StaffAvailabilityEntity(
            id = availability.id,
            staffId = availability.staffId,
            businessId = availability.businessId,
            date = availability.date
        )

        // Save the main entity
        entity = availabilityJpaRepository.save(entity)

        // Delete existing time slots
        timeSlotJpaRepository.deleteByAvailabilityId(entity.id)

        // Save new time slots
        val timeSlotEntities = availability.getTimeSlots().map { timeSlot ->
            StaffAvailabilityTimeSlotEntity(
                availabilityId = entity.id,
                startTime = timeSlot.startTime,
                endTime = timeSlot.endTime
            )
        }

        timeSlotJpaRepository.saveAll(timeSlotEntities)

        return availability // Return the original domain object
    }

    override fun findById(id: UUID): StaffDailyAvailability? {
        val entity = availabilityJpaRepository.findById(id).orElse(null) ?: return null
        val timeSlots = timeSlotJpaRepository.findByAvailabilityId(id)
        return reconstituteDomain(entity, timeSlots)
    }

    override fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): StaffDailyAvailability? {
        val entity = availabilityJpaRepository.findByStaffIdAndDate(staffId, date) ?: return null
        val timeSlots = timeSlotJpaRepository.findByAvailabilityId(entity.id)
        return reconstituteDomain(entity, timeSlots)
    }

    override fun findByStaffIdAndDateRange(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<StaffDailyAvailability> {
        val entities = availabilityJpaRepository.findByStaffIdAndDateBetween(staffId, startDate, endDate)
        return entities.map { entity ->
            val timeSlots = timeSlotJpaRepository.findByAvailabilityId(entity.id)
            reconstituteDomain(entity, timeSlots)
        }
    }

    override fun findByBusinessIdAndDate(businessId: UUID, date: LocalDate): List<StaffDailyAvailability> {
        val entities = availabilityJpaRepository.findByBusinessIdAndDate(businessId, date)
        return entities.map { entity ->
            val timeSlots = timeSlotJpaRepository.findByAvailabilityId(entity.id)
            reconstituteDomain(entity, timeSlots)
        }
    }

    override fun deleteByStaffIdAndDate(staffId: UUID, date: LocalDate) {
        val entity = availabilityJpaRepository.findByStaffIdAndDate(staffId, date) ?: return
        availabilityJpaRepository.delete(entity)
    }

    override fun existsByStaffIdAndDate(staffId: UUID, date: LocalDate): Boolean {
        return availabilityJpaRepository.existsByStaffIdAndDate(staffId, date)
    }

    private fun reconstituteDomain(
        entity: StaffAvailabilityEntity,
        timeSlotEntities: List<StaffAvailabilityTimeSlotEntity>
    ): StaffDailyAvailability {
        // Convert time slot entities to domain value objects
        val timeSlots = timeSlotEntities.map {
            TimeSlot(it.startTime, it.endTime)
        }

        // Use a proper domain factory method that won't trigger events
        return StaffDailyAvailability.reconstitute(
            id = entity.id,
            staffId = entity.staffId,
            businessId = entity.businessId,
            date = entity.date,
            timeSlots = timeSlots
        )
    }
}