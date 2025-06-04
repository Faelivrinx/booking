package com.dominikdev.booking.appointment.application

import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlot
import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlotRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Service for slot availability operations
 */
@Service
class SlotAvailabilityService(
    private val availableSlotRepository: AvailableBookingSlotRepository
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Checks if a specific slot is available
     */
    fun isSlotAvailable(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate,
        startTime: LocalTime
    ): Boolean {
        return availableSlotRepository.existsByBusinessIdAndStaffIdAndServiceIdAndDateAndStartTime(
            businessId, staffId, serviceId, date, startTime
        )
    }

    /**
     * Gets a specific available slot
     */
    fun getAvailableSlot(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate,
        startTime: LocalTime
    ): AvailableBookingSlot? {
        return availableSlotRepository.findByBusinessIdAndStaffIdAndServiceIdAndDateAndStartTime(
            businessId, staffId, serviceId, date, startTime
        )
    }

    /**
     * Finds alternative slots when the requested slot is unavailable
     */
    fun findAlternativeSlots(
        businessId: UUID,
        serviceId: UUID,
        staffId: UUID?,
        preferredDate: LocalDate,
        preferredTime: LocalTime,
        maxAlternatives: Int = 5
    ): List<AlternativeSlot> {
        val alternatives = mutableListOf<AlternativeSlot>()

        // First, try to find slots on the same day
        val sameDaySlots = findSameDayAlternatives(
            businessId, serviceId, staffId, preferredDate, preferredTime
        )
        alternatives.addAll(sameDaySlots.take(maxAlternatives))

        // If we need more alternatives, look at subsequent days
        if (alternatives.size < maxAlternatives) {
            val remainingCount = maxAlternatives - alternatives.size
            val futureDaySlots = findFutureDayAlternatives(
                businessId, serviceId, staffId, preferredDate, remainingCount
            )
            alternatives.addAll(futureDaySlots)
        }

        return alternatives
    }

    private fun findSameDayAlternatives(
        businessId: UUID,
        serviceId: UUID,
        staffId: UUID?,
        date: LocalDate,
        preferredTime: LocalTime
    ): List<AlternativeSlot> {
        val allDaySlots = if (staffId != null) {
            availableSlotRepository.findByBusinessIdAndStaffIdAndServiceIdAndDate(
                businessId, staffId, serviceId, date
            )
        } else {
            availableSlotRepository.findByBusinessIdAndServiceIdAndDateOrderByStartTime(
                businessId, serviceId, date
            )
        }

        // Sort by proximity to preferred time
        return allDaySlots
            .filter { it.startTime != preferredTime }
            .sortedBy {
                val timeDiff = it.startTime.toSecondOfDay() - preferredTime.toSecondOfDay()
                kotlin.math.abs(timeDiff)
            }
            .map { it.toAlternativeSlot() }
    }

    private fun findFutureDayAlternatives(
        businessId: UUID,
        serviceId: UUID,
        staffId: UUID?,
        startDate: LocalDate,
        maxCount: Int
    ): List<AlternativeSlot> {
        val endDate = startDate.plusDays(7)

        val futureSlots = availableSlotRepository
            .findByBusinessIdAndServiceIdAndDateBetweenOrderByDateAscStartTimeAsc(
                businessId, serviceId, startDate.plusDays(1), endDate
            )
            .filter { staffId == null || it.staffId == staffId }
            .take(maxCount)
            .map { it.toAlternativeSlot() }

        return futureSlots
    }

    private fun AvailableBookingSlot.toAlternativeSlot() = AlternativeSlot(
        date = this.date,
        startTime = this.startTime,
        endTime = this.endTime,
        staffId = this.staffId
    )
}