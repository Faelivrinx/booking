package com.dominikdev.booking.appointment.domain.model

import com.dominikdev.booking.shared.exception.DomainException

class AppointmentException(message: String, cause: Throwable? = null) : DomainException(message, cause)