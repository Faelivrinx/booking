package com.dominikdev.booking.domain.exception

abstract class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class BusinessDomainException(message: String, cause: Throwable? = null) : DomainException(message, cause)
