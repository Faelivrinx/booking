package com.dominikdev.booking.availability.api

import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlot
import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlotRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/booking-slots")
class AvailableBookingSlotsController(
    private val availableBookingSlotsRepository: AvailableBookingSlotRepository
) {
    @GetMapping("/by-service/{serviceId}")
    fun getAvailableSlotsByService(
        @PathVariable businessId: String,
        @PathVariable serviceId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<AvailableSlotResponse>> {
        val slots = availableBookingSlotsRepository.findByServiceIdAndDateOrderByStartTime(
            UUID.fromString(serviceId),
            date
        )

        return ResponseEntity.ok(slots.map { mapToResponse(it) })
    }

    @GetMapping("/by-staff/{staffId}")
    fun getAvailableSlotsByStaff(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<AvailableSlotResponse>> {
        val slots = availableBookingSlotsRepository.findByStaffIdAndDateOrderByStartTimeAsc(
            UUID.fromString(staffId),
            date
        )

        return ResponseEntity.ok(slots.map { mapToResponse(it) })
    }

    @GetMapping("/by-date")
    fun getAvailableSlotsByDate(
        @PathVariable businessId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<AvailableSlotResponse>> {
        val slots = availableBookingSlotsRepository.findByBusinessIdAndDateOrderByServiceNameAscStartTimeAsc(
            UUID.fromString(businessId),
            date
        )

        return ResponseEntity.ok(slots.map { mapToResponse(it) })
    }

    private fun mapToResponse(slot: AvailableBookingSlot): AvailableSlotResponse {
        return AvailableSlotResponse(
            id = slot.id,
            serviceId = slot.serviceId,
            staffId = slot.staffId,
            date = slot.date,
            startTime = slot.startTime,
            endTime = slot.endTime,
            serviceName = slot.serviceName,
            staffName = slot.staffName,
            servicePrice = slot.servicePrice,
            durationMinutes = slot.serviceDurationMinutes
        )
    }

    data class AvailableSlotResponse(
        val id: UUID,
        val serviceId: UUID,
        val staffId: UUID,
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val serviceName: String,
        val staffName: String,
        val servicePrice: BigDecimal?,
        val durationMinutes: Int
    )
}