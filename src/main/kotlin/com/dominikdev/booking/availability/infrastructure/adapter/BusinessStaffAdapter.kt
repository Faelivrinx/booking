package com.dominikdev.booking.availability.infrastructure.adapter


import com.dominikdev.booking.business.profile.BusinessProfileRepository
import com.dominikdev.booking.business.staff.StaffRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BusinessStaffAdapter(
    private val staffRepository: StaffRepository,
    private val businessRepository: BusinessProfileRepository
) : StaffInfoAdapter {

    override fun getStaffName(staffId: UUID): String? {
        return staffRepository.findById(staffId)
            .map { "${it.getFirstName()} ${it.getLastName()}" }
            .orElse(null)
    }

    override fun getBusinessName(businessId: UUID): String? {
        return businessRepository.findById(businessId)
            .map { it.name }
            .orElse(null)
    }
}