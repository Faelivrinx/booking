package com.dominikdev.booking.shared.domain

open class DomainException( override val message: String?, override val cause: Throwable?) : RuntimeException(message, cause) {
}