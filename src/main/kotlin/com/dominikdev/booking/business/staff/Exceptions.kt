package com.dominikdev.booking.business.staff

import com.dominikdev.booking.shared.exception.DomainException

class StaffDomainException(message: String, cause: Throwable? = null) : DomainException(message, cause)