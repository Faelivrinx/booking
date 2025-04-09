package com.dominikdev.booking.business.account.domain

import com.dominikdev.booking.shared.exception.DomainException

class BusinessDomainException(message: String, cause: Throwable? = null) : DomainException(message, cause)