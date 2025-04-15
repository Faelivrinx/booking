package com.dominikdev.booking.appointment.domain.services

import com.dominikdev.booking.appointment.domain.model.Appointment
import com.dominikdev.booking.appointment.domain.model.AppointmentDomainException
import com.dominikdev.booking.appointment.domain.model.TimeSlot
import com.dominikdev.booking.appointment.domain.ports.AppointmentRepository
import com.dominikdev.booking.appointment.domain.ports.StaffAvailabilityRepository
import com.dominikdev.booking.appointment.domain.ports.StaffServiceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class AppointmentBookingService(
    private val appointmentRepository: AppointmentRepository,
    private val staffAvailabilityRepository: StaffAvailabilityRepository,
    private val staffServiceRepository: StaffServiceRepository,
    private val serviceProvider: ServiceProvider
) {
    /**
     * Books an appointment ensuring all business rules are satisfied
     */
    fun bookAppointment(
        businessId: UUID,
        clientId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        clientTimeZone: ZoneId = ZoneId.of("UTC"),
        notes: String? = null
    ): Appointment {
        // 1. Verify the staff member can perform this service
        if (!staffServiceRepository.canStaffPerformService(staffId, serviceId)) {
            throw AppointmentDomainException("Staff member cannot perform this service")
        }

        // 2. Get service details to determine duration
        val service = serviceProvider.getServiceById(serviceId)
            ?: throw AppointmentDomainException("Service not found")

        // 3. Calculate end time based on service duration
        val endTime = startTime.plusMinutes(service.durationMinutes.toLong())

        // 4. Create the time slot
        val timeSlot = TimeSlot(startTime, endTime)

        // 5. Check staff availability
        val staffAvailability = staffAvailabilityRepository.findByStaffIdAndBusinessId(staffId, businessId)
            ?: throw AppointmentDomainException("Staff availability not found")

        if (!staffAvailability.isAvailable(date, timeSlot)) {
            throw AppointmentDomainException("Staff is not available during this time")
        }

        // 6. Check for existing appointments (prevent overlaps)
        val startDateTime = LocalDateTime.of(date, startTime)
        val endDateTime = LocalDateTime.of(date, endTime)

        val existingAppointments = appointmentRepository.findOverlappingAppointments(
            staffId,
            startDateTime,
            endDateTime
        )

        if (existingAppointments.isNotEmpty()) {
            throw AppointmentDomainException("Staff already has an appointment during this time")
        }

        // 7. Create appointment
        return Appointment.create(
            businessId = businessId,
            clientId = clientId,
            staffId = staffId,
            serviceId = serviceId,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            notes = notes
        )
    }
}