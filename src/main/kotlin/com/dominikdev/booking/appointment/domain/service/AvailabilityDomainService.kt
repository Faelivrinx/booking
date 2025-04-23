package com.dominikdev.booking.appointment.domain.service

import com.dominikdev.booking.appointment.domain.model.*
import com.dominikdev.booking.appointment.domain.repository.AppointmentRepository
import com.dominikdev.booking.appointment.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class AvailabilityDomainService(
    private val availabilityRepository: StaffAvailabilityRepository,
    private val appointmentRepository: AppointmentRepository,
    private val eventPublisher: DomainEventPublisher
) {
    /**
     * Set or update staff availability for a specific date
     */
    fun setAvailability(
        staffId: UUID,
        businessId: UUID,
        date: LocalDate,
        timeSlots: List<TimeSlot>
    ) : StaffDailyAvailability {
        // Check if there are any existing appointments for this date
        val existingAppointments = fetchExistingAppointments(staffId, date)

        // Validate that new time slots can accommodate all existing appointments
        if (existingAppointments.isNotEmpty()) {
            validateAppointmentsFit(existingAppointments, timeSlots)
        }

        // Create or update staff availability
        val availability = availabilityRepository.findByStaffIdAndDate(staffId, date)
            ?: StaffDailyAvailability(
                staffId = staffId,
                businessId = businessId,
                date = date
            )

        // set availability
        availability.setAvailability(timeSlots)

        // Save and publish events
        val savedAvailability = availabilityRepository.save(availability)

        eventPublisher.publish(
            StaffDailyAvailabilityUpdatedEvent(
                staffId = staffId,
                businessId = businessId,
                date = date
            )
        )

        return savedAvailability
    }

    /**
     * Delete staff availability for a specific date
     */
    fun deleteStaffAvailability(staffId: UUID, businessId: UUID, date: LocalDate) {
        // Check if there are any existing appointments for this date
        val existingAppointments = fetchExistingAppointments(staffId, date)

        if (existingAppointments.isNotEmpty()) {
            throw AvailabilityException("Cannot delete availability for a day with existing appointments")
        }

        // Delete the availability
        availabilityRepository.deleteByStaffIdAndDate(staffId, date)
    }

    /**
     * Add a single time slot to staff availability
     */
    fun addTimeSlot(
        staffId: UUID,
        businessId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): StaffDailyAvailability {
        // Get or create availability
        val availability = availabilityRepository.findByStaffIdAndDate(staffId, date)
            ?: StaffDailyAvailability(
                staffId = staffId,
                businessId = businessId,
                date = date
            )

        // Add time slot
        availability.addTimeSlot(startTime, endTime)

        // Save and return
        return availabilityRepository.save(availability)
    }

    /**
     * Remove a single time slot from staff availability
     */
    fun removeTimeSlot(
        staffId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): StaffDailyAvailability? {
        // Get availability
        val availability = availabilityRepository.findByStaffIdAndDate(staffId, date)
            ?: return null

        // Create time slot
        val timeSlot = TimeSlot(startTime, endTime)

        // Check if removing this time slot would affect any existing appointments
        val appointments = fetchExistingAppointments(staffId, date)

        for (appointment in appointments) {
            val appointmentSlot = appointment.getTimeSlot()
            if (timeSlot.contains(appointmentSlot)) {
                throw AvailabilityException("Cannot remove time slot that contains existing appointments")
            }
        }

        // Remove the time slot
        availability.removeTimeSlot(timeSlot)

        // If no time slots remain, delete the entire availability record
        if (availability.isEmpty()) {
            availabilityRepository.deleteByStaffIdAndDate(staffId, date)
            return null
        }

        // Save and return
        return availabilityRepository.save(availability)
    }


    private fun fetchExistingAppointments(staffId: UUID, date: LocalDate) : List<Appointment> =
        appointmentRepository.findByStaffIdAndDateRange(staffId, date, date)
        .filter { it.getStatus() != AppointmentStatus.CANCELLED }

    /**
     * Helper method to validate that all appointments fit within the new time slots
     */
    private fun validateAppointmentsFit(
        appointments: List<Appointment>,
        timeSlots: List<TimeSlot>
    ) {
        for (appointment in appointments) {
            val appointmentSlot = appointment.getTimeSlot()

            val fits = timeSlots.any { it.contains(appointmentSlot) }

            if (!fits) {
                throw AvailabilityException(
                    "Cannot update availability: Some existing appointments would be outside available hours. " +
                            "Appointment at ${appointmentSlot.startTime}-${appointmentSlot.endTime} doesn't fit."
                )
            }
        }
    }
}