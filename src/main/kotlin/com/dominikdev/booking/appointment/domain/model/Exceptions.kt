package com.dominikdev.booking.appointment.domain.model


class AvailabilityException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class BookingException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class OverlappingTimeSlots(message: String) : RuntimeException(message)