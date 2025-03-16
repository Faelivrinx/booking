package com.dominikdev.booking.shared.exception

abstract class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
