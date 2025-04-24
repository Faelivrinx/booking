package com.dominikdev.booking.availability.infrastructure.adapter

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Adapter implementation that uses the business context's staff service association
 */
@Component
class BusinessStaffServiceAdapter(
    private val staffServiceAssociationService: StaffServiceAssociationService
) : StaffServiceAdapter {

    override fun getServicesForStaff(staffId: UUID): List<UUID> {
        return staffServiceAssociationService.getServicesForStaff(staffId)
    }

    override fun getStaffForService(serviceId: UUID): List<UUID> {
        return staffServiceAssociationService.getStaffForService(serviceId)
    }

    override fun canStaffPerformService(staffId: UUID, serviceId: UUID): Boolean {
        return staffServiceAssociationService.canStaffPerformService(staffId, serviceId)
    }
}