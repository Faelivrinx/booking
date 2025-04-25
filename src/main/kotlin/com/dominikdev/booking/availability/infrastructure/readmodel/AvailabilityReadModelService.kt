package com.dominikdev.booking.availability.infrastructure.readmodel

import com.dominikdev.booking.availability.domain.model.StaffAvailabilityUpdatedEvent
import com.dominikdev.booking.availability.domain.model.TimeSlot
import com.dominikdev.booking.availability.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.availability.infrastructure.adapter.ServiceInfoAdapter
import com.dominikdev.booking.availability.infrastructure.adapter.StaffInfoAdapter
import com.dominikdev.booking.availability.infrastructure.adapter.StaffServiceAdapter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class AvailabilityReadModelService(
    private val availableBookingSlotRepository: AvailableBookingSlotRepository,
    private val staffAvailabilityRepository: StaffAvailabilityRepository,
    private val staffServiceAdapter: StaffServiceAdapter,
    private val serviceInfoAdapter: ServiceInfoAdapter,
    private val staffInfoAdapter: StaffInfoAdapter
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
        @EventListener
        @Transactional
        fun handleStaffAvailabilityUpdated(event: StaffAvailabilityUpdatedEvent) {
            logger.debug {
                "Processing availability change for staff ${event.staffId} on ${event.date}:" +
                        "\nPrevious time slots: ${event.previousTimeSlots}" +
                        "\nCurrent time slots: ${event.currentTimeSlots}" +
                        "\nDetected added slots: ${event.added}" +
                        "\nDetected removed slots: ${event.removed}"
            }
            // Skip processing if no actual changes
            if (!event.hasChanges) {
                return
            }

            val staffId = event.staffId
            val businessId = event.businessId
            val date = event.date

            // Get services this staff member can perform
            val services = staffServiceAdapter.getServicesForStaff(staffId)
            if (services.isEmpty()) {
                // If staff has no services, just remove any slots that might exist
                availableBookingSlotRepository.deleteByBusinessIdAndStaffIdAndDate(businessId, staffId, date)
                return
            }

            // Approach 1: Surgical update of affected time slots
            if (event.added.isNotEmpty() || event.removed.isNotEmpty()) {
                // Remove slots that correspond to removed availability
                event.removed.forEach { removedSlot ->
                    availableBookingSlotRepository.deleteSlotInTimeRange(
                        businessId = businessId,
                        staffId = staffId,
                        date = date,
                        startTime = removedSlot.startTime,
                        endTime = removedSlot.endTime
                    )
                }

                // Add new slots only for added availability
                val newBookingSlots = mutableListOf<AvailableBookingSlot>()

                for (serviceId in services) {
                    val serviceDuration = serviceInfoAdapter.getServiceDuration(serviceId) ?: continue

                    // Only process new time slots
                    for (slot in event.added) {
                        createBookingSlotsForTimeSlot(
                            businessId = businessId,
                            staffId = staffId,
                            serviceId = serviceId,
                            date = date,
                            timeSlot = slot,
                            serviceDuration = serviceDuration,
                            slots = newBookingSlots
                        )
                    }
                }

                if (newBookingSlots.isNotEmpty()) {
                    availableBookingSlotRepository.saveAll(newBookingSlots)
                }
            }
        }

    private fun createBookingSlotsForTimeSlot(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate,
        timeSlot: TimeSlot,
        serviceDuration: Int,
        slots: MutableList<AvailableBookingSlot>
    ) {
        // Skip slots shorter than the service duration
        val slotDurationMinutes = java.time.Duration.between(
            timeSlot.startTime,
            timeSlot.endTime
        ).toMinutes().toInt()

        if (slotDurationMinutes < serviceDuration) {
            return
        }

        // Calculate the number of possible slots within this time range
        val numberOfPossibleSlots = (slotDurationMinutes - serviceDuration) / serviceDuration + 1

        // Create bookable slots at exact service duration intervals
        for (i in 0 until numberOfPossibleSlots) {
            val startTime = timeSlot.startTime.plusMinutes((i * serviceDuration).toLong())
            val endTime = startTime.plusMinutes(serviceDuration.toLong())

            // Double-check that the end time doesn't exceed the available time slot
            if (!endTime.isAfter(timeSlot.endTime)) {
                slots.add(
                    AvailableBookingSlot(
                        businessId = businessId,
                        serviceId = serviceId,
                        staffId = staffId,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        serviceDurationMinutes = serviceDuration
                    )
                )
            }
        }
    }
}