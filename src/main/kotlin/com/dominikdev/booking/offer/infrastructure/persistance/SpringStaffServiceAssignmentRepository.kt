package com.dominikdev.booking.offer.infrastructure.persistance

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

internal interface SpringStaffServiceAssignmentRepository : CrudRepository<StaffServiceAssignmentEntity, String> {

    fun findByStaffId(staffId: UUID): List<StaffServiceAssignmentEntity>

    fun findByServiceId(serviceId: UUID): List<StaffServiceAssignmentEntity>

    fun findByBusinessId(businessId: UUID): List<StaffServiceAssignmentEntity>

    fun findByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): StaffServiceAssignmentEntity?

    fun existsByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): Boolean

    fun deleteByStaffIdAndServiceId(staffId: UUID, serviceId: UUID)

    fun deleteByStaffId(staffId: UUID)

    fun deleteByServiceId(serviceId: UUID)

    @Query("DELETE FROM staff_service_assignments WHERE staff_id = :staffId")
    fun deleteAllByStaffId(@Param("staffId") staffId: UUID)
}