package com.dominikdev.booking.appointment.domain.ports

import com.dominikdev.booking.appointment.domain.model.StaffAvailability
import java.util.*

interface StaffAvailabilityRepository {
    fun save(staffAvailability: StaffAvailability): StaffAvailability
    fun findById(id: UUID): StaffAvailability?
    fun findByStaffIdAndBusinessId(staffId: UUID, businessId: UUID): StaffAvailability?
}