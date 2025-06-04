package com.dominikdev.booking.appointment.application

import com.dominikdev.booking.appointment.domain.model.Appointment
import com.dominikdev.booking.appointment.domain.model.AppointmentException
import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import com.dominikdev.booking.appointment.domain.repository.AppointmentRepository
import com.dominikdev.booking.availability.infrastructure.adapter.ServiceInfoAdapter
import com.dominikdev.booking.availability.infrastructure.adapter.StaffInfoAdapter
import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlotRepository
import com.dominikdev.booking.clients.identity.ClientIdentityService
import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Application service for appointment management
 */
@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val availableSlotRepository: AvailableBookingSlotRepository,
    private val serviceInfoAdapter: ServiceInfoAdapter,
    private val staffInfoAdapter: StaffInfoAdapter,
    private val clientIdentityService: ClientIdentityService,
    private val eventPublisher: DomainEventPublisher
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Books a new appointment for a client
     * Uses PostgreSQL exclusion constraint to prevent double bookings
     */
    @Transactional
    fun bookAppointment(command: BookAppointmentCommand): AppointmentDTO {
        logger.info { "Booking appointment for client ${command.clientId} with staff ${command.staffId}" }

        try {
            // Create the appointment
            val appointment = Appointment.schedule(
                businessId = command.businessId,
                clientId = command.clientId,
                staffId = command.staffId,
                serviceId = command.serviceId,
                date = command.date,
                startTime = command.startTime,
                endTime = command.endTime,
                notes = command.notes
            )

            // Save the appointment - database constraint will handle overlapping check
            val savedAppointment = appointmentRepository.save(appointment)

            // Publish domain events
            publishEvents(appointment)

            // Return the appointment details
            return mapToDTO(savedAppointment)
        } catch (e: DataIntegrityViolationException) {
            logger.error(e) { "Failed to book appointment due to data integrity violation" }
            throw AppointmentException("This time slot is no longer available. Please select another slot.", e)
        } catch (e: Exception) {
            logger.error(e) { "Failed to book appointment: ${e.message}" }
            throw AppointmentException("Failed to book appointment: ${e.message}", e)
        }
    }

    /**
     * Gets appointments for a client
     */
    @Transactional(readOnly = true)
    fun getClientAppointments(clientId: UUID): List<AppointmentDTO> {
        // Fetch appointments from repository
        val appointments = appointmentRepository.findByClientId(clientId)

        // Map to DTOs
        return appointments.map { mapToDTO(it) }
    }

    /**
     * Gets upcoming appointments for a client
     */
    @Transactional(readOnly = true)
    fun getClientUpcomingAppointments(clientId: UUID): List<AppointmentDTO> {
        // Get upcoming appointments (SCHEDULED or CONFIRMED status)
        val statuses = listOf(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED)
        val appointments = appointmentRepository.findByClientIdAndStatuses(clientId, statuses)
            .sortedBy { it.date.atTime(it.startTime) }

        return appointments.map { mapToDTO(it) }
    }

    /**
     * Gets appointments for a client in a specific date range
     */
    @Transactional(readOnly = true)
    fun getClientAppointmentsByDateRange(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AppointmentDTO> {
        // Fetch appointments from repository
        val appointments = appointmentRepository.findByClientIdAndDateRange(
            clientId, startDate, endDate
        )

        // Map to DTOs
        return appointments.map { mapToDTO(it) }
    }

    /**
     * Gets a specific appointment by ID
     */
    @Transactional(readOnly = true)
    fun getAppointmentById(appointmentId: UUID): AppointmentDTO? {
        // Fetch appointment from repository
        val appointment = appointmentRepository.findById(appointmentId) ?: return null

        // Map to DTO
        return mapToDTO(appointment)
    }

    /**
     * Gets staff's appointments for a date
     */
    @Transactional(readOnly = true)
    fun getStaffAppointmentsForDate(staffId: UUID, date: LocalDate): List<AppointmentDTO> {
        val appointments = appointmentRepository.findByStaffIdAndDate(staffId, date)
        return appointments.map { mapToDTO(it) }
    }

    /**
     * Cancels an appointment
     */
    @Transactional
    fun cancelAppointment(command: CancelAppointmentCommand): AppointmentDTO {
        // Fetch the appointment
        val appointment = appointmentRepository.findById(command.appointmentId)
            ?: throw AppointmentException("Appointment not found")

        // Verify client owns this appointment
        if (appointment.clientId != command.clientId) {
            throw AppointmentException("Access denied to this appointment")
        }

        // Cancel the appointment
        val cancelledAppointment = appointment.cancel(command.reason)

        // Save the updated appointment
        val savedAppointment = appointmentRepository.save(cancelledAppointment)

        // Publish domain events
        publishEvents(cancelledAppointment)

        // Return the updated appointment
        return mapToDTO(savedAppointment)
    }

    /**
     * Staff confirms an appointment
     */
    @Transactional
    fun confirmAppointment(command: ConfirmAppointmentCommand): AppointmentDTO {
        // Fetch the appointment
        val appointment = appointmentRepository.findById(command.appointmentId)
            ?: throw AppointmentException("Appointment not found")

        // Confirm the appointment
        val confirmedAppointment = appointment.confirm()

        // Save the updated appointment
        val savedAppointment = appointmentRepository.save(confirmedAppointment)

        // Publish domain events
        publishEvents(confirmedAppointment)

        // Return the updated appointment
        return mapToDTO(savedAppointment)
    }

    /**
     * Marks an appointment as completed
     */
    @Transactional
    fun completeAppointment(command: CompleteAppointmentCommand): AppointmentDTO {
        // Fetch the appointment
        val appointment = appointmentRepository.findById(command.appointmentId)
            ?: throw AppointmentException("Appointment not found")

        // Complete the appointment
        val completedAppointment = appointment.complete()

        // Save the updated appointment
        val savedAppointment = appointmentRepository.save(completedAppointment)

        // Publish domain events
        publishEvents(completedAppointment)

        // Return the updated appointment
        return mapToDTO(savedAppointment)
    }

    /**
     * Marks a client as no-show for an appointment
     */
    @Transactional
    fun markAsNoShow(command: NoShowAppointmentCommand): AppointmentDTO {
        // Fetch the appointment
        val appointment = appointmentRepository.findById(command.appointmentId)
            ?: throw AppointmentException("Appointment not found")

        // Mark as no-show
        val noShowAppointment = appointment.markAsNoShow()

        // Save the updated appointment
        val savedAppointment = appointmentRepository.save(noShowAppointment)

        // Publish domain events
        publishEvents(noShowAppointment)

        // Return the updated appointment
        return mapToDTO(savedAppointment)
    }

    /**
     * Gets all pending appointments for a business
     */
    @Transactional(readOnly = true)
    fun getPendingAppointments(businessId: UUID): List<AppointmentDTO> {
        val now = LocalDate.now()
        val appointments = appointmentRepository.findPendingAppointmentsByBusiness(businessId, now)
        return appointments.map { mapToDTO(it) }
    }

    /**
     * Gets all appointments for a staff member
     */
    @Transactional(readOnly = true)
    fun getAllStaffAppointments(staffId: UUID): List<AppointmentDTO> {
        val appointments = appointmentRepository.findByStaffId(staffId)
        return appointments.map { mapToDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getStaffAppointmentsForDateRange(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AppointmentDTO> {
        val appointments = appointmentRepository.findByStaffIdAndDateRange(staffId, startDate, endDate)
        return appointments.map { mapToDTO(it) }
    }

    /**
     * Gets upcoming appointments for a staff member
     */
    @Transactional(readOnly = true)
    fun getStaffUpcomingAppointments(staffId: UUID): List<AppointmentDTO> {
        val today = LocalDate.now()
        val statuses = listOf(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED)
        val appointments = appointmentRepository.findByStaffIdAndStatusesAndDateAfter(
            staffId, statuses, today.minusDays(1)
        )
        return appointments.map { mapToDTO(it) }
            .sortedBy { it.date.atTime(it.startTime) }
    }

    /**
     * Staff cancels an appointment
     * Different from client cancellation - may have different business rules
     */
    @Transactional
    fun staffCancelAppointment(command: StaffCancelAppointmentCommand): AppointmentDTO {
        logger.info { "Staff ${command.staffId} cancelling appointment ${command.appointmentId}" }

        // Fetch the appointment
        val appointment = appointmentRepository.findById(command.appointmentId)
            ?: throw AppointmentException("Appointment not found")

        // Verify staff owns this appointment
        if (appointment.staffId != command.staffId) {
            throw AppointmentException("Staff can only cancel their own appointments")
        }

        // Cancel the appointment
        val cancelledAppointment = appointment.cancel(command.reason)

        // Save the updated appointment
        val savedAppointment = appointmentRepository.save(cancelledAppointment)

        // Publish domain events
        publishEvents(cancelledAppointment)

        // Log for audit
        logger.info {
            "Staff cancellation completed - appointment: ${command.appointmentId}, " +
                    "client: ${appointment.clientId}, date: ${appointment.date}"
        }

        return mapToDTO(savedAppointment)
    }

    /**
     * Publishes all domain events from an appointment
     */
    private fun publishEvents(appointment: Appointment) {
        val events = appointment.getEvents()
        if (events.isNotEmpty()) {
            events.forEach { eventPublisher.publish(it) }
            appointment.clearEvents()
        }
    }

    /**
     * Maps a domain Appointment to an AppointmentDTO
     */
    private fun mapToDTO(appointment: Appointment): AppointmentDTO {
        // Get additional information from adapters
        val serviceName = serviceInfoAdapter.getServiceName(appointment.serviceId)
        val staffName = staffInfoAdapter.getStaffName(appointment.staffId)
        val businessName = staffInfoAdapter.getBusinessName(appointment.businessId)

        // Get client information
        val clientName = try {
            val client = clientIdentityService.getClientById(appointment.clientId)
            "${client.firstName} ${client.lastName}"
        } catch (e: Exception) {
            null
        }

        return AppointmentDTO(
            id = appointment.id,
            businessId = appointment.businessId,
            businessName = businessName,
            clientId = appointment.clientId,
            clientName = clientName,
            staffId = appointment.staffId,
            staffName = staffName,
            serviceId = appointment.serviceId,
            serviceName = serviceName,
            date = appointment.date,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            durationMinutes = appointment.durationMinutes(),
            status = appointment.status,
            notes = appointment.notes,
            createdAt = appointment.createdAt,
            updatedAt = appointment.updatedAt
        )
    }
}

