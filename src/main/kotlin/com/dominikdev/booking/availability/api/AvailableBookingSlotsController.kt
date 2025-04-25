package com.dominikdev.booking.availability.api

import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlot
import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlotRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/available-slots")
class AvailableBookingSlotsController(
    private val availableBookingSlotRepository: AvailableBookingSlotRepository
) {
    @GetMapping("/service/{serviceId}")
    fun getAvailableSlotsForService(
        @PathVariable businessId: String,
        @PathVariable serviceId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
    ): ResponseEntity<AvailableSlotsResponse> {
        val businessUuid = UUID.fromString(businessId)
        val serviceUuid = UUID.fromString(serviceId)

        // Single date query
        if (date != null) {
            val slots = availableBookingSlotRepository
                .findByBusinessIdAndServiceIdAndDateOrderByStartTime(
                    businessUuid,
                    serviceUuid,
                    date
                ).map { mapToSlot(it) }

            return ResponseEntity.ok(AvailableSlotsResponse(slots))
        }

        // Default to today if no date parameters provided
        val today = LocalDate.now()
        val slots = availableBookingSlotRepository
            .findByBusinessIdAndServiceIdAndDateOrderByStartTime(
                businessUuid,
                serviceUuid,
                today
            ).map { mapToSlot(it) }

        return ResponseEntity.ok(AvailableSlotsResponse(slots))
    }

    @GetMapping("/staff/{staffId}/service/{serviceId}")
    fun getAvailableSlotsForStaffAndService(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @PathVariable serviceId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<AvailableSlotsResponse> {
        val businessUuid = UUID.fromString(businessId)
        val staffUuid = UUID.fromString(staffId)
        val serviceUuid = UUID.fromString(serviceId)
        val queryDate = date ?: LocalDate.now()

        // Get slots by business, staff and service
        val slots = availableBookingSlotRepository
            .findByBusinessIdAndStaffIdAndServiceIdAndDate(
                businessUuid,
                staffUuid,
                serviceUuid,
                queryDate
            ).map { mapToSlot(it) }

        return ResponseEntity.ok(AvailableSlotsResponse(slots))
    }

    @GetMapping("/service/{serviceId}/days-with-slots")
    fun getDaysWithAvailableSlots(
        @PathVariable businessId: String,
        @PathVariable serviceId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam(defaultValue = "7") days: Int
    ): ResponseEntity<DaysWithSlotsResponse> {
        val businessUuid = UUID.fromString(businessId)
        val serviceUuid = UUID.fromString(serviceId)

        // Cap the maximum days to prevent excessive queries
        val adjustedDays = days.coerceAtMost(60)

        // Calculate end date
        val endDate = startDate.plusDays(adjustedDays.toLong() - 1)

        // Get all days with available slots
        val daysWithSlots = availableBookingSlotRepository
            .findDaysWithAvailableSlotsByBusinessIdAndServiceId(
                businessUuid,
                serviceUuid,
                startDate,
                endDate
            )

        // Create result for all days in range
        val result = (0 until adjustedDays).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            DayAvailability(
                date = date,
                hasSlots = daysWithSlots.contains(date)
            )
        }

        return ResponseEntity.ok(DaysWithSlotsResponse(result))
    }

    /**
     * Maps an entity to the simplified response slot
     */
    private fun mapToSlot(entity: AvailableBookingSlot): AvailableTimeSlot {
        return AvailableTimeSlot(
            id = entity.id,
            date = entity.date,
            startTime = entity.startTime,
            endTime = entity.endTime,
            staffId = entity.staffId,
            durationMinutes = entity.serviceDurationMinutes
        )
    }

    /**
     * Response classes
     */
    data class AvailableTimeSlot(
        val id: UUID,
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val staffId: UUID,
        val durationMinutes: Int
    )

    data class AvailableSlotsResponse(
        val availableSlots: List<AvailableTimeSlot>
    )

    data class DayAvailability(
        val date: LocalDate,
        val hasSlots: Boolean
    )

    data class DaysWithSlotsResponse(
        val days: List<DayAvailability>
    )
}