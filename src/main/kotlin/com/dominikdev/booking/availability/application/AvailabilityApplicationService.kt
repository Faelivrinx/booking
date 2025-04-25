package com.dominikdev.booking.availability.application

import com.dominikdev.booking.availability.domain.model.StaffDailyAvailability
import com.dominikdev.booking.availability.domain.model.TimeSlot
import com.dominikdev.booking.availability.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Service
class AvailabilityApplicationService(
    private val availabilityRepository: StaffAvailabilityRepository,
    private val eventPublisher: DomainEventPublisher
) {
    @Transactional
    fun setStaffAvailability(
        staffId: UUID,
        businessId: UUID,
        date: LocalDate,
        timeSlots: List<TimeSlot>
    ): StaffDailyAvailability {
        val availability = availabilityRepository.findByStaffIdAndDate(staffId, date)
            ?: StaffDailyAvailability(
                staffId = staffId,
                businessId = businessId,
                date = date
            )

        // Get change result
        val changeResult = availability.setAvailability(timeSlots)

        // Only save if there are actual changes
        if (changeResult.added.isNotEmpty() || changeResult.removed.isNotEmpty()) {
            val savedAvailability = availabilityRepository.save(availability)

            // Publish a single event
            availability.getEvents().forEach { eventPublisher.publish(it) }
            availability.clearEvents()

            return savedAvailability
        }
        return availability
    }

    @Transactional(readOnly = true)
    fun getAvailability(staffId: UUID, date: LocalDate): List<TimeSlot> {
        return availabilityRepository.findByStaffIdAndDate(staffId, date)?.getTimeSlots() ?: emptyList()
    }

    @Transactional(readOnly = true)
    fun getAvailabilityForDateRange(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, List<TimeSlot>> {
        val availabilities = availabilityRepository.findByStaffIdAndDateRange(staffId, startDate, endDate)
        return availabilities.associate { it.date to it.getTimeSlots() }
    }
}