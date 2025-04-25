package com.dominikdev.booking.availability.api

import com.dominikdev.booking.availability.application.AvailabilityApplicationService
import com.dominikdev.booking.availability.domain.model.TimeSlot
import com.dominikdev.booking.shared.infrastructure.security.SecurityContextUtils
import com.dominikdev.booking.shared.infrastructure.security.ValidateStaffBelongsToBusiness
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@RestController
@RequestMapping("/businesses/staff/{staffId}/availability")
class StaffAvailabilityController(
    private val availabilityService: AvailabilityApplicationService,
    private val securityContextUtils: SecurityContextUtils
) {
    @PostMapping("/{date}")
    @ValidateStaffBelongsToBusiness
    fun setAvailability(
        @PathVariable staffId: String,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody request: SetAvailabilityRequest
    ): ResponseEntity<AvailabilityResponse> {
        val timeSlots = request.timeSlots.map { TimeSlot(it.startTime, it.endTime) }

        val availability = availabilityService.setStaffAvailability(
            staffId = UUID.fromString(staffId),
            businessId = securityContextUtils.getBusinessIdOrThrow(),
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
    ): ResponseEntity<AvailabilityTimeSlotsResponse> {
        val timeSlots = availabilityService.getAvailability(UUID.fromString(staffId), date)

        if (timeSlots.isEmpty()) {
            return ResponseEntity.notFound().build()
        }

        val response = AvailabilityTimeSlotsResponse(
            timeSlots = timeSlots.map { TimeSlotDto(it.startTime, it.endTime) }
        )

        return ResponseEntity.ok(response)
    }

    // DTOs
    data class TimeSlotDto(
        val startTime: LocalTime,
        val endTime: LocalTime
    )

    data class SetAvailabilityRequest(
        val timeSlots: List<TimeSlotDto>
    )

    data class AvailabilityResponse(
        val id: UUID,
        val staffId: UUID,
        val businessId: UUID,
        val date: LocalDate,
        val timeSlots: List<TimeSlotDto>
    )

    data class AvailabilityTimeSlotsResponse(
        val timeSlots: List<TimeSlotDto>
    )
}