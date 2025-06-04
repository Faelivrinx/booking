package com.dominikdev.booking.availability.application

import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlot
import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlotRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID


/**
 * Facade for the Availability bounded context
 * Provides a clean interface for other contexts to query availability information
 */
@Component
class AvailabilityFacade(
    private val availableBookingSlotRepository: AvailableBookingSlotRepository
) {

    /**
     * Checks if a specific time slot is available for booking
     */
    @Transactional(readOnly = true)
    fun isSlotAvailable(query: SlotAvailabilityQuery): Boolean {
        return availableBookingSlotRepository.existsByBusinessIdAndStaffIdAndServiceIdAndDateAndStartTime(
            businessId = query.businessId,
            staffId = query.staffId,
            serviceId = query.serviceId,
            date = query.date,
            startTime = query.startTime
        )
    }

    /**
     * Gets detailed information about an available slot
     */
    @Transactional(readOnly = true)
    fun getAvailableSlot(query: SlotAvailabilityQuery): AvailableSlotDTO? {
        val slot = availableBookingSlotRepository.findByBusinessIdAndStaffIdAndServiceIdAndDateAndStartTime(
            businessId = query.businessId,
            staffId = query.staffId,
            serviceId = query.serviceId,
            date = query.date,
            startTime = query.startTime
        )

        return slot?.let { mapToDTO(it) }
    }

    /**
     * Finds alternative slots when the requested one is unavailable
     */
    @Transactional(readOnly = true)
    fun findAlternativeSlots(query: AlternativeSlotsQuery): List<AvailableSlotDTO> {
        val alternatives = mutableListOf<AvailableSlotDTO>()

        // First, try to find slots on the same day
        val sameDaySlots = findSameDayAlternatives(query)
        alternatives.addAll(sameDaySlots.take(query.maxResults))

        // If we need more alternatives, look at subsequent days
        if (alternatives.size < query.maxResults && query.searchFutureDays) {
            val remainingCount = query.maxResults - alternatives.size
            val futureDaySlots = findFutureDayAlternatives(query, remainingCount)
            alternatives.addAll(futureDaySlots)
        }

        return alternatives
    }

    /**
     * Gets all available slots for a service on a specific date
     */
    @Transactional(readOnly = true)
    fun getAvailableSlotsForService(
        businessId: UUID,
        serviceId: UUID,
        date: LocalDate
    ): List<AvailableSlotDTO> {
        return availableBookingSlotRepository
            .findByBusinessIdAndServiceIdAndDateOrderByStartTime(businessId, serviceId, date)
            .map { mapToDTO(it) }
    }

    /**
     * Gets available slots for a specific staff member
     */
    @Transactional(readOnly = true)
    fun getAvailableSlotsForStaff(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate
    ): List<AvailableSlotDTO> {
        return availableBookingSlotRepository
            .findByBusinessIdAndStaffIdAndServiceIdAndDate(businessId, staffId, serviceId, date)
            .map { mapToDTO(it) }
    }

    private fun findSameDayAlternatives(query: AlternativeSlotsQuery): List<AvailableSlotDTO> {
        val allDaySlots = if (query.staffId != null) {
            availableBookingSlotRepository.findByBusinessIdAndStaffIdAndServiceIdAndDate(
                query.businessId, query.staffId, query.serviceId, query.preferredDate
            )
        } else {
            availableBookingSlotRepository.findByBusinessIdAndServiceIdAndDateOrderByStartTime(
                query.businessId, query.serviceId, query.preferredDate
            )
        }

        // Sort by proximity to preferred time if provided
        return if (query.preferredTime != null) {
            allDaySlots
                .filter { it.startTime != query.preferredTime }
                .sortedBy {
                    val timeDiff = it.startTime.toSecondOfDay() - query.preferredTime.toSecondOfDay()
                    kotlin.math.abs(timeDiff)
                }
                .map { mapToDTO(it) }
        } else {
            allDaySlots.map { mapToDTO(it) }
        }
    }

    private fun findFutureDayAlternatives(
        query: AlternativeSlotsQuery,
        maxCount: Int
    ): List<AvailableSlotDTO> {
        val startDate = query.preferredDate.plusDays(1)
        val endDate = query.preferredDate.plusDays(query.daysToSearch)

        return availableBookingSlotRepository
            .findByBusinessIdAndServiceIdAndDateBetweenOrderByDateAscStartTimeAsc(
                query.businessId, query.serviceId, startDate, endDate
            )
            .filter { query.staffId == null || it.staffId == query.staffId }
            .take(maxCount)
            .map { mapToDTO(it) }
    }

    private fun mapToDTO(slot: AvailableBookingSlot): AvailableSlotDTO {
        return AvailableSlotDTO(
            id = slot.id,
            businessId = slot.businessId,
            serviceId = slot.serviceId,
            staffId = slot.staffId,
            date = slot.date,
            startTime = slot.startTime,
            endTime = slot.endTime,
            durationMinutes = slot.serviceDurationMinutes
        )
    }
}

// Query objects for the facade
data class SlotAvailabilityQuery(
    val businessId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime
)

data class AlternativeSlotsQuery(
    val businessId: UUID,
    val serviceId: UUID,
    val staffId: UUID? = null,
    val preferredDate: LocalDate,
    val preferredTime: LocalTime? = null,
    val maxResults: Int = 5,
    val searchFutureDays: Boolean = true,
    val daysToSearch: Long = 7
)

// DTO for external contexts
data class AvailableSlotDTO(
    val id: UUID,
    val businessId: UUID,
    val serviceId: UUID,
    val staffId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Int
)