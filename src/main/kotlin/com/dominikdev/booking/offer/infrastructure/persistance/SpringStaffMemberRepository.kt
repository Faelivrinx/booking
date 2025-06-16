package com.dominikdev.booking.offer.infrastructure.persistance

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

internal interface SpringStaffMemberRepository : CrudRepository<StaffMemberEntity, UUID> {

    fun findByKeycloakId(keycloakId: String): StaffMemberEntity?

    fun findByBusinessId(businessId: UUID): List<StaffMemberEntity>

    @Query("SELECT * FROM staff_members WHERE business_id = :businessId AND is_active = true")
    fun findActiveByBusinessId(@Param("businessId") businessId: UUID): List<StaffMemberEntity>

    fun findByBusinessIdAndEmail(businessId: UUID, email: String): StaffMemberEntity?

    fun existsByBusinessIdAndEmail(businessId: UUID, email: String): Boolean

    fun existsByKeycloakId(keycloakId: String): Boolean

    @Query("UPDATE staff_members SET is_active = false, updated_at = :updatedAt WHERE id = :id")
    fun deactivateById(@Param("id") id: UUID, @Param("updatedAt") updatedAt: java.time.LocalDateTime)
}