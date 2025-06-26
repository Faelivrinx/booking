package com.dominikdev.booking.offer.infrastructure.persistance

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface SpringStaffServiceAssignmentRepository : CrudRepository<StaffServiceAssignmentEntity, String> {

    fun findByStaffId(staffId: UUID): List<StaffServiceAssignmentEntity>

    fun findByServiceId(serviceId: UUID): List<StaffServiceAssignmentEntity>

    fun findByBusinessId(businessId: UUID): List<StaffServiceAssignmentEntity>

    fun findByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): StaffServiceAssignmentEntity?

    fun existsByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): Boolean

    @Modifying
    @Transactional
    fun deleteByStaffIdAndServiceId(staffId: UUID, serviceId: UUID)

    @Modifying
    @Transactional
    fun deleteByStaffId(staffId: UUID)

    @Modifying
    @Transactional
    fun deleteByServiceId(serviceId: UUID)

    @Modifying
    @Transactional
    @Query("DELETE FROM StaffServiceAssignmentEntity s WHERE s.staffId = :staffId")
    fun deleteAllByStaffId(@Param("staffId") staffId: UUID)
}