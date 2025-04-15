package com.dominikdev.booking.appointment.domain.ports

import com.dominikdev.booking.business.staff.StaffService
import java.util.*

interface StaffServiceRepository {
    fun save(staffService: StaffService): StaffService
    fun findByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): StaffService?
    fun findByStaffId(staffId: UUID): List<StaffService>
    fun findByServiceId(serviceId: UUID): List<StaffService>
    fun canStaffPerformService(staffId: UUID, serviceId: UUID): Boolean
}