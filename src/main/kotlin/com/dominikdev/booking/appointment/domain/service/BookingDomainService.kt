package com.dominikdev.booking.appointment.domain.service

import com.dominikdev.booking.appointment.domain.model.Appointment
import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import com.dominikdev.booking.appointment.domain.model.BookingException
import com.dominikdev.booking.appointment.domain.model.TimeSlot
import com.dominikdev.booking.appointment.domain.repository.AppointmentRepository
import com.dominikdev.booking.appointment.domain.repository.ServiceRepository
import com.dominikdev.booking.appointment.domain.repository.StaffAvailabilityRepository
import com.dominikdev.booking.appointment.domain.repository.StaffServiceAllocationRepository
import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/**
 * Domain service for booking appointments
 * Contains core business logic for the booking process
 */
class BookingDomainService(
    private val appointmentRepository: AppointmentRepository,
    private val availabilityRepository: StaffAvailabilityRepository,
    private val staffServiceRepository: StaffServiceAllocationRepository,
    private val serviceRepository: ServiceRepository,
    private val eventPublisher: DomainEventPublisher
) {
    /**
     * Book a new appointment
     */
    fun bookAppointment(
        businessId: UUID,
        serviceId: UUID,
        staffId: UUID,
        clientId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        clientTimeZone: ZoneId = ZoneId.systemDefault(),
        notes: String? = null
    ): Appointment {
        // 1. Validate that staff can perform this service
        if (!staffServiceRepository.canStaffPerformService(staffId, serviceId)) {
            throw BookingException("Staff member cannot perform this service")
        }

        // 2. Get service duration
        val serviceDuration = serviceRepository.getServiceDuration(serviceId)
            ?: throw BookingException("Service not found or duration not specified")

        // 3. Calculate end time
        val endTime = startTime.plusMinutes(serviceDuration.toLong())

        // 4. Check if staff has availability for this date
        val availability = availabilityRepository.findByStaffIdAndDate(staffId, date)
            ?: throw BookingException("Staff is not available on this date")

        // 5. Check if the time slot is within staff availability
        val timeSlot = TimeSlot(startTime, endTime)
        if (!availability.isAvailable(timeSlot)) {
            throw BookingException("Staff is not available during this time slot")
        }

        // 6. Check for overlapping appointments
        val hasOverlap = appointmentRepository.existsOverlappingAppointment(
            staffId = staffId,
            date = date,
            startTime = startTime,
            endTime = endTime
        )

        if (hasOverlap) {
            throw BookingException("Another appointment already exists during this time")
        }

        // 7. Create appointment
        val appointment = Appointment.create(
            businessId = businessId,
            serviceId = serviceId,
            staffId = staffId,
            clientId = clientId,
            date = date,
            startTime = startTime,
            endTime = endTime,
            timeZone = clientTimeZone,
        )

        // 8. Update staff availability (cut out the booked time slot)
        availability.applyAppointment(timeSlot)
        availabilityRepository.save(availability)

        // 9. Save appointment
        val savedAppointment = appointmentRepository.save(appointment)

        // 10. Publish domain events
        appointment.getEvents().forEach { eventPublisher.publish(it) }
        appointment.clearEvents()

        availability.getEvents().forEach { eventPublisher.publish(it) }
        availability.clearEvents()

        return savedAppointment
    }

    /**
     * Cancel an appointment
     */
    fun cancelAppointment(appointmentId: UUID, reason: String? = null): Appointment {
        // 1. Find the appointment
        val appointment = appointmentRepository.findById(appointmentId)
            ?: throw BookingException("Appointment not found")

        // 2. Cancel the appointment
        if (!appointment.cancel(reason)) {
            throw BookingException("Appointment cannot be cancelled (current status: ${appointment.getStatus()})")
        }

        // 3. Save the updated appointment
        val savedAppointment = appointmentRepository.save(appointment)

        // 4. Publish domain events
        appointment.getEvents().forEach { eventPublisher.publish(it) }
        appointment.clearEvents()

        return savedAppointment
    }

    /**
     * Mark an appointment as completed
     */
    fun completeAppointment(appointmentId: UUID): Appointment {
        // 1. Find the appointment
        val appointment = appointmentRepository.findById(appointmentId)
            ?: throw BookingException("Appointment not found")

        // 2. Mark as completed
        if (!appointment.markAsCompleted()) {
            throw BookingException("Appointment cannot be marked as completed (current status: ${appointment.getStatus()})")
        }

        // 3. Save the updated appointment
        val savedAppointment = appointmentRepository.save(appointment)

        // 4. Publish domain events
        appointment.getEvents().forEach { eventPublisher.publish(it) }
        appointment.clearEvents()

        return savedAppointment
    }

    /**
     * Mark an appointment as no-show
     */
    fun markNoShow(appointmentId: UUID): Appointment {
        // 1. Find the appointment
        val appointment = appointmentRepository.findById(appointmentId)
            ?: throw BookingException("Appointment not found")

        // 2. Mark as no-show
        if (!appointment.markAsNoShow()) {
            throw BookingException("Appointment cannot be marked as no-show (current status: ${appointment.getStatus()})")
        }

        // 3. Save the updated appointment
        val savedAppointment = appointmentRepository.save(appointment)

        // 4. Publish domain events
        appointment.getEvents().forEach { eventPublisher.publish(it) }
        appointment.clearEvents()

        return savedAppointment
    }

}