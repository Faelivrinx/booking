package com.dominikdev.booking.appointment.infrastructure.event

import com.dominikdev.booking.appointment.domain.model.AppointmentCancelledEvent
import com.dominikdev.booking.appointment.domain.model.AppointmentCompletedEvent
import com.dominikdev.booking.appointment.domain.model.AppointmentConfirmedEvent
import com.dominikdev.booking.appointment.domain.model.AppointmentNoShowEvent
import com.dominikdev.booking.appointment.domain.model.AppointmentScheduledEvent
import com.dominikdev.booking.availability.infrastructure.readmodel.AvailableBookingSlotRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Event listener for appointment-related events
 */
@Component
class AppointmentEventListener(
    private val availableSlotRepository: AvailableBookingSlotRepository
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handle appointment scheduled event
     * Removes the booked slot from available slots
     */
    @EventListener
    fun handleAppointmentScheduled(event: AppointmentScheduledEvent) {
        logger.info {
            "Appointment scheduled - business: ${event.businessId}, " +
                    "client: ${event.clientId}, date: ${event.date}, " +
                    "time: ${event.startTime}-${event.endTime}"
        }

        // Remove the reserved time slot from available slots
        // This is a safety measure as the exclusion constraint should prevent any
        // overlapping appointments, but we still want to keep the available slots
        // list accurate
        availableSlotRepository.deleteSlotInTimeRange(
            businessId = event.businessId,
            staffId = event.staffId,
            date = event.date,
            startTime = event.startTime,
            endTime = event.endTime
        )

        //TODO: send notification here
    }

    /**
     * Handle appointment confirmed event
     */
    @EventListener
    fun handleAppointmentConfirmed(event: AppointmentConfirmedEvent) {
        logger.info {
            "Appointment confirmed - ID: ${event.appointmentId}, " +
                    "business: ${event.businessId}, client: ${event.clientId}"
        }

        //TODO: send notification here
    }

    /**
     * Handle appointment completed event
     */
    @EventListener
    fun handleAppointmentCompleted(event: AppointmentCompletedEvent) {
        logger.info {
            "Appointment completed - ID: ${event.appointmentId}, " +
                    "business: ${event.businessId}, client: ${event.clientId}"
        }

    }

    /**
     * Handle appointment no-show event
     */
    @EventListener
    fun handleAppointmentNoShow(event: AppointmentNoShowEvent) {
        logger.info {
            "Client no-show for appointment - ID: ${event.appointmentId}, " +
                    "business: ${event.businessId}, client: ${event.clientId}"
        }
    }
}