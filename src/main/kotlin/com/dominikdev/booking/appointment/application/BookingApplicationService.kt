package com.dominikdev.booking.appointment.application

import com.dominikdev.booking.appointment.domain.model.AppointmentException
import com.dominikdev.booking.availability.application.AlternativeSlotsQuery
import com.dominikdev.booking.availability.application.AvailabilityFacade
import com.dominikdev.booking.availability.application.AvailableSlotDTO
import com.dominikdev.booking.availability.application.SlotAvailabilityQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Application service for booking operations
 * Uses the AvailabilityFacade to interact with the availability bounded context
 */
@Service
class BookingApplicationService(
    private val appointmentService: AppointmentService,
    private val availabilityFacade: AvailabilityFacade // Using facade from availability context
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Attempts to book an appointment with comprehensive error handling
     */
    @Transactional
    fun bookAppointmentWithValidation(command: ValidatedBookingCommand): BookingResult {
        logger.info {
            "Processing validated booking - client: ${command.clientId}, " +
                    "service: ${command.serviceId}, slot: ${command.date} ${command.startTime}"
        }

        // Query availability through the facade
        val availabilityQuery = SlotAvailabilityQuery(
            businessId = command.businessId,
            staffId = command.staffId,
            serviceId = command.serviceId,
            date = command.date,
            startTime = command.startTime
        )

        val isAvailable = availabilityFacade.isSlotAvailable(availabilityQuery)

        if (!isAvailable) {
            logger.warn { "Slot no longer available for booking attempt" }

            val alternatives = findAlternatives(command)

            return BookingResult.SlotUnavailable(
                message = "This time slot is no longer available",
                alternativeSlots = alternatives
            )
        }

        return try {
            // Attempt to book the appointment
            val bookingCommand = BookAppointmentCommand(
                businessId = command.businessId,
                clientId = command.clientId,
                staffId = command.staffId,
                serviceId = command.serviceId,
                date = command.date,
                startTime = command.startTime,
                endTime = command.endTime,
                notes = command.notes
            )

            val appointment = appointmentService.bookAppointment(bookingCommand)

            logger.info { "Successfully booked appointment: ${appointment.id}" }

            BookingResult.Success(appointment = appointment)

        } catch (e: DataIntegrityViolationException) {
            // This happens when the exclusion constraint is violated
            logger.warn { "Booking failed due to constraint violation - concurrent booking" }

            val alternatives = findAlternatives(command)

            BookingResult.SlotUnavailable(
                message = "This time slot was just booked by another user. Please select a different time.",
                alternativeSlots = alternatives
            )

        } catch (e: AppointmentException) {
            logger.error(e) { "Booking failed with domain exception: ${e.message}" }
            BookingResult.ValidationError(message = e.message ?: "Booking failed")

        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during booking: ${e.message}" }
            BookingResult.SystemError(message = "An unexpected error occurred. Please try again.")
        }
    }

    /**
     * Validates if a specific slot is available
     */
    @Transactional(readOnly = true)
    fun validateSlotAvailability(query: SlotAvailabilityQuery): SlotAvailabilityResult {
        val slot = availabilityFacade.getAvailableSlot(query)

        return SlotAvailabilityResult(
            available = slot != null,
            slot = slot?.let {
                AvailableSlotInfo(
                    id = it.id,
                    date = it.date,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    staffId = it.staffId,
                    durationMinutes = it.durationMinutes
                )
            }
        )
    }

    /**
     * Finds alternative slots using the availability facade
     */
    private fun findAlternatives(command: ValidatedBookingCommand): List<AlternativeSlot> {
        val alternativesQuery = AlternativeSlotsQuery(
            businessId = command.businessId,
            serviceId = command.serviceId,
            staffId = command.staffId,
            preferredDate = command.date,
            preferredTime = command.startTime,
            maxResults = 5,
            searchFutureDays = true,
            daysToSearch = 7
        )

        return availabilityFacade.findAlternativeSlots(alternativesQuery)
            .map { slot ->
                AlternativeSlot(
                    date = slot.date,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    staffId = slot.staffId
                )
            }
    }
}

// Command and Result objects remain the same
data class ValidatedBookingCommand(
    val businessId: UUID,
    val clientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val notes: String? = null
)

// Result types using sealed classes for type safety
sealed class BookingResult {
    data class Success(val appointment: AppointmentDTO) : BookingResult()
    data class SlotUnavailable(
        val message: String,
        val alternativeSlots: List<AlternativeSlot>
    ) : BookingResult()
    data class ValidationError(val message: String) : BookingResult()
    data class SystemError(val message: String) : BookingResult()
}

data class SlotAvailabilityResult(
    val available: Boolean,
    val slot: AvailableSlotInfo?
)

data class AvailableSlotInfo(
    val id: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val staffId: UUID,
    val durationMinutes: Int
)

data class AlternativeSlot(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val staffId: UUID
)