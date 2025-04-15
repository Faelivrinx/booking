package com.dominikdev.booking.appointment.domain.model

/**
 * Appointment statuses
 */
enum class AppointmentStatus {
    SCHEDULED,    // Initial state when appointment is created
    CONFIRMED,    // Confirmed by the business
    COMPLETED,    // Service was provided
    CANCELLED,    // Cancelled by either party
    NO_SHOW       // Client didn't show up
}