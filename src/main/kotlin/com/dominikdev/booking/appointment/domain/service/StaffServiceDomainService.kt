package com.dominikdev.booking.appointment.domain.service

import com.dominikdev.booking.appointment.domain.model.StaffServiceAllocation
import com.dominikdev.booking.appointment.domain.model.StaffServiceAssociationUpdatedEvent
import com.dominikdev.booking.appointment.domain.repository.StaffServiceAllocationRepository
import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import java.util.*

class StaffServiceDomainService(
    private val staffServiceRepository: StaffServiceAllocationRepository,
    private val eventPublisher: DomainEventPublisher
) {
    /**
     * Associate a staff member with a service
     */
    fun assignServiceToStaff(
        staffId: UUID,
        serviceId: UUID,
        businessId: UUID
    ): StaffServiceAllocation {
        // Check if the association already exists
        val existingAllocation = staffServiceRepository.findByStaffIdAndServiceId(staffId, serviceId)

        if (existingAllocation != null) {
            return existingAllocation
        }

        val allocation = StaffServiceAllocation(
            staffId = staffId,
            serviceId = serviceId,
            businessId = businessId,
        )

        // Save and publish events
        val saved = staffServiceRepository.save(allocation)

        eventPublisher.publish(
            StaffServiceAssociationUpdatedEvent(
                staffId = staffId,
                serviceId = serviceId,
                businessId = businessId,
                active = true
            )
        )

        return saved
    }

    /**
     * Remove a service from a staff member
     */
    fun removeServiceFromStaff(
        staffId: UUID,
        serviceId: UUID,
        businessId: UUID
    ): Boolean {
        val existingAllocation = staffServiceRepository.findByStaffIdAndServiceId(staffId, serviceId)
            ?: return false

        staffServiceRepository.deleteById(existingAllocation.id)

        return true
    }

    /**
    * Get all services a staff member can perform
    */
    fun getServicesForStaff(staffId: UUID): List<UUID> {
        return staffServiceRepository.getServicesForStaff(staffId)
    }

    /**
     * Get all staff who can perform a service
     */
    fun getStaffForService(serviceId: UUID): List<UUID> {
        return staffServiceRepository.getStaffForService(serviceId)
    }

    /**
     * Check if a staff member can perform a specific service
     */
    fun canStaffPerformService(staffId: UUID, serviceId: UUID): Boolean {
        return staffServiceRepository.canStaffPerformService(staffId, serviceId)
    }
}