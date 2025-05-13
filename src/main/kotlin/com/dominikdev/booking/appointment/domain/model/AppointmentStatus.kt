package com.dominikdev.booking.appointment.domain.model

enum class AppointmentStatus {
    SCHEDULED,  // Initial status when appointment is created
    CONFIRMED,  // Appointment confirmed (e.g., by staff or automatically)
    CANCELLED,  // Appointment has been cancelled
    COMPLETED,  // Appointment has been completed
    NO_SHOW     // Client did not show up
}