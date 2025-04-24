package com.dominikdev.booking.shared.infrastructure.security

import com.dominikdev.booking.business.profile.BusinessProfileRepository
import com.dominikdev.booking.business.service.ServiceRepository
import com.dominikdev.booking.business.staff.StaffRepository
import com.dominikdev.booking.shared.exception.DomainException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BusinessResourceValidator(
    private val staffRepository: StaffRepository,
    private val serviceRepository: ServiceRepository,
    private val businessRepository: BusinessProfileRepository
) {
    /**
     * Validates that a business exists
     */
    fun validateBusinessExists(businessId: UUID) {
        if (!businessRepository.existsById(businessId)) {
            throw BusinessSecurityException("Business with ID $businessId not found")
        }
    }

    /**
     * Validates that a staff member belongs to a business
     */
    fun validateStaffBelongsToBusiness(staffId: UUID, businessId: UUID) {
        val staff = staffRepository.findById(staffId)
            .orElseThrow { BusinessSecurityException("Staff member with ID $staffId not found") }

        if (staff.businessId != businessId) {
            throw BusinessSecurityException("Staff member does not belong to this business")
        }
    }

    /**
     * Validates that a service belongs to a business
     */
    fun validateServiceBelongsToBusiness(serviceId: UUID, businessId: UUID) {
        val service = serviceRepository.findById(serviceId)
            .orElseThrow { BusinessSecurityException("Service with ID $serviceId not found") }

        if (service.businessId != businessId) {
            throw BusinessSecurityException("Service does not belong to this business")
        }
    }

    /**
     * Validates that all services belong to a business
     */
    fun validateServicesBelongToBusiness(serviceIds: List<UUID>, businessId: UUID) {
        for (serviceId in serviceIds) {
            validateServiceBelongsToBusiness(serviceId, businessId)
        }
    }
}

/**
 * Custom exception for business resource security violations
 */
class BusinessSecurityException(message: String) : DomainException(message)
