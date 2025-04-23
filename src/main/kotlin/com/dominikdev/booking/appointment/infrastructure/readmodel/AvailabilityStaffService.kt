package com.dominikdev.booking.appointment.infrastructure.readmodel

import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import com.dominikdev.booking.appointment.domain.model.StaffDailyAvailabilityUpdatedEvent
import com.dominikdev.booking.appointment.domain.model.TimeSlot
import com.dominikdev.booking.appointment.domain.repository.AppointmentRepository
import com.dominikdev.booking.appointment.domain.repository.ServiceRepository
import com.dominikdev.booking.appointment.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.appointment.domain.repository.StaffServiceAllocationRepository
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Service responsible for keeping the read model in sync with domain events
 * and providing efficient query methods for client-facing operations
 */
@Service
class AvailabilityReadModelService(
    private val availableBookingSlotRepository: AvailableBookingSlotRepository,
    private val staffDailyScheduleRepository: StaffDailyScheduleRepository,
    private val clientAppointmentViewRepository: ClientAppointmentViewRepository,
    private val staffAvailabilityRepository: StaffAvailabilityRepository,
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository,
    private val staffServiceRepository: StaffServiceAllocationRepository,
) {
    /**
     * Event handler for staff availability updates
     */
    @EventListener
    @Transactional
    fun handleStaffAvailabilityUpdated(event: StaffDailyAvailabilityUpdatedEvent) {
        // 1. Get updated availability from domain model
        val staffId = event.staffId
        val businessId = event.businessId
        val date = event.date

        val availability = staffAvailabilityRepository.findByStaffIdAndDate(staffId, date)

        // 2. Delete existing availability slots for this staff and date
        availableBookingSlotRepository.deleteByStaffIdAndDate(staffId, date)
        staffDailyScheduleRepository.deleteByStaffIdAndDate(staffId, date)

        if (availability == null) {
            return // No availability to sync
        }

        // 3. Get services this staff member can perform
        val services = staffServiceRepository.getServicesForStaff(staffId)
        if (services.isEmpty()) {
            return // No services to create slots for
        }

        // 4. Get existing appointments for this staff and date
        val appointments = appointmentRepository.findByStaffIdAndDateRange(
            staffId = staffId,
            startDate = date,
            endDate = date
        )

        // 5. Generate daily schedule entries (both available and booked)
        val scheduleEntries = mutableListOf<StaffDailySchedule>()

        // Get staff name
        val staffName = staffInfoService.getStaffName(staffId) ?: "Staff Member"

        // Add available slots
        for (slot in availability.getTimeSlots()) {
            // Check if slot overlaps with any appointment
            val overlappingAppointments = appointments.filter { appointment ->
                val appointmentSlot = appointment.getTimeSlot()
                val appointmentTime = TimeSlot(
                    appointmentSlot.startTime,
                    appointmentSlot.endTime
                )

                val availabilitySlot = TimeSlot(
                    slot.startTime,
                    slot.endTime
                )

                appointmentTime.overlaps(availabilitySlot)
            }

            if (overlappingAppointments.isEmpty()) {
                // This is a fully available slot
                scheduleEntries.add(
                    StaffDailySchedule(
                        businessId = businessId,
                        staffId = staffId,
                        date = date,
                        startTime = slot.startTime,
                        endTime = slot.endTime,
                        isAvailable = true
                    )
                )
            } else {
                // Need to handle possible multiple appointments in this slot
                // Sort appointments by start time
                val sortedAppointments = overlappingAppointments.sortedBy { it.getStartTime() }

                var currentTime = slot.startTime

                for (appointment in sortedAppointments) {
                    val appointmentStart = appointment.getStartTime()
                    val appointmentEnd = appointment.getEndTime()

                    // Add available slot before appointment if there's a gap
                    if (appointmentStart.isAfter(currentTime)) {
                        scheduleEntries.add(
                            StaffDailySchedule(
                                businessId = businessId,
                                staffId = staffId,
                                date = date,
                                startTime = currentTime,
                                endTime = appointmentStart,
                                isAvailable = true
                            )
                        )
                    }

                    // Add the appointment slot
                    if (appointment.getStatus() != AppointmentStatus.CANCELLED) {
                        val serviceName = serviceRepository.getServiceName(appointment.serviceId) ?: "Service"
                        val clientName = clientInfoService.getClientName(appointment.clientId) ?: "Client"

                        scheduleEntries.add(
                            StaffDailySchedule(
                                businessId = businessId,
                                staffId = staffId,
                                date = date,
                                startTime = appointmentStart,
                                endTime = appointmentEnd,
                                isAvailable = false,
                                appointmentId = appointment.id,
                                serviceName = serviceName,
                                clientName = clientName
                            )
                        )
                    }

                    // Update current time to end of this appointment
                    currentTime = appointmentEnd
                }

                // Add available slot after last appointment if there's remaining time
                if (currentTime.isBefore(slot.endTime)) {
                    scheduleEntries.add(
                        StaffDailySchedule(
                            businessId = businessId,
                            staffId = staffId,
                            date = date,
                            startTime = currentTime,
                            endTime = slot.endTime,
                            isAvailable = true
                        )
                    )
                }
            }
        }

        // 6. Save schedule entries
        staffDailyScheduleRepository.saveAll(scheduleEntries)

        // 7. Generate available booking slots for each service
        val bookingSlots = mutableListOf<AvailableBookingSlot>()

        for (serviceId in services) {
            val serviceInfo = serviceRepository.getServiceDuration(serviceId) ?: continue
            val serviceName = serviceRepository.getServiceName(serviceId) ?: "Service"
            val servicePrice = serviceRepository.getServicePrice(serviceId)
            val serviceDuration = serviceInfo

            // Only use free slots (isAvailable == true)
            val freeSlots = scheduleEntries.filter { it.isAvailable }

            for (slot in freeSlots) {
                // Skip slots shorter than the service duration
                val slotDurationMinutes = java.time.Duration.between(
                    slot.startTime,
                    slot.endTime
                ).toMinutes().toInt()

                if (slotDurationMinutes < serviceDuration) {
                    continue
                }

                // Create bookable slots at 15-minute intervals
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

                    // Move to next 15-minute increment
                    currentStartTime = currentStartTime.plusMinutes(15)
                }
            }
        }

        // 8. Save available booking slots
        if (bookingSlots.isNotEmpty()) {
            availableBookingSlotRepository.saveAll(bookingSlots)
        }
    }

    /**
     * Event handler for appointment creation
     */
    @EventListener
    @Transactional
    fun handleAppointmentCreated(event: AppointmentCreatedEvent) {
        // 1. Remove booking slots that would overlap with this appointment
        availableBookingSlotRepository.deleteSlotInTimeRange(
            staffId = event.staffId,
            date = event.date,
            startTime = event.startTime,
            endTime = event.endTime
        )

        // 2. Update staff daily schedule
        updateStaffScheduleForAppointment(event.staffId, event.businessId, event.date)

        // 3. Create or update client appointment view
        updateClientAppointmentView(event.appointmentId)
    }

    /**
     * Event handler for appointment cancellation
     */
    @EventListener
    @Transactional
    fun handleAppointmentCancelled(event: AppointmentCancelledEvent) {
        // When an appointment is cancelled, we need to regenerate availability
        updateStaffScheduleForAppointment(event.staffId, event.businessId, event.date)

        // Update client appointment view
        updateClientAppointmentView(event.appointmentId)
    }

    /**
     * Helper method to update staff schedule after appointment changes
     */
    private fun updateStaffScheduleForAppointment(
        staffId: UUID,
        businessId: UUID,
        date: LocalDate
    ) {
        // Since this is complex logic involving multiple aggregates,
        // we'll just trigger a full regeneration of the read model for this date
        val event = StaffDailyAvailabilityUpdatedEvent(
            staffId = staffId,
            businessId = businessId,
            date = date
        )

        handleStaffAvailabilityUpdated(event)
    }

    /**
     * Helper method to update client appointment view
     */
    private fun updateClientAppointmentView(appointmentId: UUID) {
        // Get appointment from domain model
        val appointment = appointmentRepository.findById(appointmentId) ?: return

        // Get required information
        val serviceName = serviceRepository.getServiceName(appointment.serviceId) ?: "Service"
        val servicePrice = serviceRepository.getServicePrice(appointment.serviceId)
        val staffName = staffInfoService.getStaffName(appointment.staffId) ?: "Staff Member"
        val businessName = staffInfoService.getBusinessName(appointment.businessId) ?: "Business"

        // Create or update client appointment view
        val view = ClientAppointmentView(
            id = appointment.id,
            clientId = appointment.clientId,
            businessId = appointment.businessId,
            businessName = businessName,
            serviceId = appointment.serviceId,
            serviceName = serviceName,
            staffId = appointment.staffId,
            staffName = staffName,
            date = appointment.date,
            startTime = appointment.getStartTime(),
            endTime = appointment.getEndTime(),
            status = appointment.getStatus().name,
            price = servicePrice,
            createdAt = appointment.createdAt,
            updatedAt = appointment.createdAt // Using createdAt as a fallback
        )

        clientAppointmentViewRepository.save(view)
    }

    /**
     * Generate available slots for a time period
     * This is useful for initialization and repair of the read model
     */
    @Transactional
    fun generateAvailableSlotsForPeriod(
        businessId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            // Get all staff for this business
            val staffAvailabilities = staffAvailabilityRepository.findByBusinessIdAndDate(businessId, currentDate)

            for (availability in staffAvailabilities) {
                // Trigger read model update for each staff member
                handleStaffAvailabilityUpdated(
                    StaffDailyAvailabilityUpdatedEvent(
                        staffId = availability.staffId,
                        businessId = businessId,
                        date = currentDate
                    )
                )
            }

            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * Scheduled task to clean up old available slots
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun cleanupOldAvailableSlots() {
        val today = LocalDate.now()

        // Custom query to delete slots older than today
        // Implement with repository or EntityManager based on your preference
    }
}