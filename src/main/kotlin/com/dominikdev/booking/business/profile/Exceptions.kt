package com.dominikdev.booking.business.profile

import com.dominikdev.booking.shared.exception.DomainException

class BusinessProfileException(message: String, cause: Throwable? = null) : DomainException(message, cause)
