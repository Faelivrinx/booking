package com.dominikdev.booking.offer.domain

import java.util.*

interface StaffServiceAssignmentRepository {
    fun save(assignment: StaffServiceAssignment): StaffServiceAssignment
    fun findByStaffId(staffId: UUID): List<StaffServiceAssignment>
    fun findByServiceId(serviceId: UUID): List<StaffServiceAssignment>
    fun findByBusinessId(businessId: UUID): List<StaffServiceAssignment>
    fun findByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): StaffServiceAssignment?
    fun existsByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): Boolean
    fun deleteByStaffIdAndServiceId(staffId: UUID, serviceId: UUID)
    fun deleteByStaffId(staffId: UUID)
    fun deleteByServiceId(serviceId: UUID)
    fun replaceStaffServices(staffId: UUID, serviceIds: List<UUID>, businessId: UUID)
}