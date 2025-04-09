package com.dominikdev.booking.business.service

import com.dominikdev.booking.shared.exception.DomainException

class ServicesException(message: String, cause: Throwable? = null) : DomainException(message, cause)
