package com.dominikdev.booking.availability.domain.model

import com.dominikdev.booking.shared.exception.DomainException

class OverlappingTimeSlots(message: String) : DomainException(message)