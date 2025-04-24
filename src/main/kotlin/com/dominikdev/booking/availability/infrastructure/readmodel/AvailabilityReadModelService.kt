package com.dominikdev.booking.availability.infrastructure.readmodel

import com.dominikdev.booking.availability.domain.model.StaffDailyAvailabilityUpdatedEvent
import com.dominikdev.booking.availability.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.availability.infrastructure.adapter.ServiceInfoAdapter
import com.dominikdev.booking.availability.infrastructure.adapter.StaffInfoAdapter
import com.dominikdev.booking.availability.infrastructure.adapter.StaffServiceAdapter
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AvailabilityReadModelService(
    private val availableBookingSlotRepository: AvailableBookingSlotRepository,
    private val staffAvailabilityRepository: StaffAvailabilityRepository,
    private val staffServiceAdapter: StaffServiceAdapter,
    private val serviceInfoAdapter: ServiceInfoAdapter,
    private val staffInfoAdapter: StaffInfoAdapter
) {
    @EventListener
    @Transactional
    fun handleStaffAvailabilityUpdated(event: StaffDailyAvailabilityUpdatedEvent) {
        // 1. Get updated availability from domain model
        val staffId = event.staffId
        val businessId = event.businessId
        val date = event.date

        val availability = staffAvailabilityRepository.findByStaffIdAndDate(staffId, date)

        // 2. Delete existing booking slots for this staff and date
        availableBookingSlotRepository.deleteByStaffIdAndDate(staffId, date)

        if (availability == null) {
            return // No availability to process
        }

        // 3. Get services this staff member can perform
        val services = staffServiceAdapter.getServicesForStaff(staffId)
        if (services.isEmpty()) {
            return // No services to create slots for
        }

        // 4. Get staff name
        val staffName = staffInfoAdapter.getStaffName(staffId) ?: "Staff Member"

        // 5. Generate available booking slots for each service
        val bookingSlots = mutableListOf<AvailableBookingSlot>()

        for (serviceId in services) {
            val serviceDuration = serviceInfoAdapter.getServiceDuration(serviceId) ?: continue
            val serviceName = serviceInfoAdapter.getServiceName(serviceId) ?: "Service"
            val servicePrice = serviceInfoAdapter.getServicePrice(serviceId)

            // Create bookable slots for each availability time slot
            for (slot in availability.getTimeSlots()) {
                // Skip slots shorter than the service duration
                val slotDurationMinutes = java.time.Duration.between(
                    slot.startTime,
                    slot.endTime
                ).toMinutes().toInt()

                if (slotDurationMinutes < serviceDuration) {
                    continue
                }

                // Create bookable slots with intervals matching the service duration
                var currentStartTime = slot.startTime
                while (true) {
                    val potentialEndTime = currentStartTime.plusMinutes(serviceDuration.toLong())

                    if (potentialEndTime.isAfter(slot.endTime)) {
                        break // Can't fit another slot
                    }

                    bookingSlots.add(
                        AvailableBookingSlot(
                            businessId = businessId,
                            serviceId = serviceId,
                            staffId = staffId,
                            date = date,
                            startTime = currentStartTime,
                            endTime = potentialEndTime,
                            serviceDurationMinutes = serviceDuration,
                            serviceName = serviceName,
                            staffName = staffName,
                            servicePrice = servicePrice
                        )
                    )

                    // Move to the next interval based on the service duration
                    currentStartTime = currentStartTime.plusMinutes(serviceDuration.toLong())
                }
            }
        }

        // 6. Save available booking slots
        if (bookingSlots.isNotEmpty()) {
            availableBookingSlotRepository.saveAll(bookingSlots)
        }
    }
}