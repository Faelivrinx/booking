package com.dominikdev.booking.offer.domain

import com.dominikdev.booking.shared.domain.DomainException
import java.util.*

open class OfferException(message: String, cause: Throwable? = null) : DomainException(message, cause)

class BusinessNotFoundException(businessId: UUID) : OfferException("Business not found: $businessId")
class ServiceNotFoundException(serviceId: UUID) : OfferException("Service not found: $serviceId")
class StaffMemberNotFoundException(staffId: UUID) : OfferException("Staff member not found: $staffId")
class DuplicateServiceException(serviceName: String) : OfferException("Service '$serviceName' already exists in this business")
class DuplicateStaffMemberException(email: String) : OfferException("Staff member with email '$email' already exists in this business")
class InvalidBusinessOperationException(message: String) : OfferException(message)
class UnauthorizedBusinessAccessException(message: String) : OfferException(message)