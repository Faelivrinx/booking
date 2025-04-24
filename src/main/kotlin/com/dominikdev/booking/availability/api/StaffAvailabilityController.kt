package com.dominikdev.booking.availability.api

import com.dominikdev.booking.availability.application.AvailabilityApplicationService
import com.dominikdev.booking.availability.domain.model.TimeSlot
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/staff/{staffId}/availability")
class StaffAvailabilityController(
    private val availabilityService: AvailabilityApplicationService
) {
    @PostMapping("/{date}")
    fun setAvailability(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody request: SetAvailabilityRequest
    ): ResponseEntity<AvailabilityResponse> {
        val timeSlots = request.timeSlots.map { TimeSlot(it.startTime, it.endTime) }

        val availability = availabilityService.setStaffAvailability(
            staffId = UUID.fromString(staffId),
            businessId = UUID.fromString(businessId),
            date = date,
            timeSlots = timeSlots
        )

        val response = AvailabilityResponse(
            id = availability.id,
            staffId = availability.staffId,
            businessId = availability.businessId,
            date = availability.date,
            timeSlots = availability.getTimeSlots().map { TimeSlotDto(it.startTime, it.endTime) }
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{date}")
    fun getAvailability(
        @PathVariable staffId: String,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<AvailabilityResponse> {
        val timeSlots = availabilityService.getAvailability(UUID.fromString(staffId), date)

        if (timeSlots.isEmpty()) {
            return ResponseEntity.notFound().build()
        }

        val response = AvailabilityResponse(
            id = UUID.randomUUID(), // Placeholder since we don't have the aggregate
            staffId = UUID.fromString(staffId),
            businessId = UUID.randomUUID(), // Placeholder since we don't have the aggregate
            date = date,
            timeSlots = timeSlots.map { TimeSlotDto(it.startTime, it.endTime) }
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{date}/slots")
    fun addTimeSlot(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody request: AddTimeSlotRequest
    ): ResponseEntity<AvailabilityResponse> {
        val availability = availabilityService.addTimeSlot(
            staffId = UUID.fromString(staffId),
            businessId = UUID.fromString(businessId),
            date = date,
            startTime = request.startTime,
            endTime = request.endTime
        )

        val response = AvailabilityResponse(
            id = availability.id,
            staffId = availability.staffId,
            businessId = availability.businessId,
            date = availability.date,
            timeSlots = availability.getTimeSlots().map { TimeSlotDto(it.startTime, it.endTime) }
        )

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{date}/slots")
    fun removeTimeSlot(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody request: DeleteTimeSlotRequest
    ): ResponseEntity<Void> {
        val removed = availabilityService.removeTimeSlot(
            staffId = UUID.fromString(staffId),
            date = date,
            startTime = request.startTime,
            endTime = request.endTime
        )

        return if (removed) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // DTOs
    data class TimeSlotDto(
        val startTime: LocalTime,
        val endTime: LocalTime
    )

    data class SetAvailabilityRequest(
        val timeSlots: List<TimeSlotDto>
    )

    data class AddTimeSlotRequest(
        val startTime: LocalTime,
        val endTime: LocalTime
    )

    data class DeleteTimeSlotRequest(
        val startTime: LocalTime,
        val endTime: LocalTime
    )

    data class AvailabilityResponse(
        val id: UUID,
        val staffId: UUID,
        val businessId: UUID,
        val date: LocalDate,
        val timeSlots: List<TimeSlotDto>
    )
}