package com.dominikdev.booking.business.staff

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface StaffServiceRepository : JpaRepository<StaffServiceAssociation, UUID> {

    @Query("SELECT ssa.serviceId FROM StaffServiceAssociation ssa WHERE ssa.staffId = :staffId")
    fun findServicesByStaffId(@Param("staffId") staffId: UUID): List<UUID>

    @Query("SELECT ssa.staffId FROM StaffServiceAssociation ssa WHERE ssa.serviceId = :serviceId")
    fun findStaffByServiceId(@Param("serviceId") serviceId: UUID): List<UUID>

    fun existsByStaffIdAndServiceId(staffId: UUID, serviceId: UUID) : Boolean

    @Modifying
    @Query("DELETE FROM StaffServiceAssociation ssa WHERE ssa.staffId = :staffId AND ssa.serviceId = :serviceId")
    fun deleteByStaffIdAndServiceId(@Param("staffId") staffId: UUID, @Param("serviceId") serviceId: UUID)

    @Query("SELECT COUNT(ssa) FROM StaffServiceAssociation ssa WHERE ssa.staffId = :staffId")
    fun countServicesByStaffId(@Param("staffId") staffId: UUID): Int
}