package com.dominikdev.booking.identity.domain

import com.dominikdev.booking.shared.domain.DomainException

open class IdentityException(message: String, cause: Throwable? = null) : DomainException(message, cause)
class UserNotFoundException(identifier: String) : IdentityException("User not found: $identifier")
class DuplicateUserException(email: String) : IdentityException("User with email $email already exists")
class InvalidUserDataException(message: String) : IdentityException(message)
class UnauthorizedException(message: String) : IdentityException(message)