package com.dominikdev.booking.clients.identity

import com.dominikdev.booking.shared.exception.DomainException

class ClientDomainException(message: String, cause: Throwable? = null) : DomainException(message, cause)

