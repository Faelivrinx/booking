package com.dominikdev.booking.offer.infrastructure.persistance

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface SpringStaffMemberRepository : CrudRepository<StaffMemberEntity, UUID> {

    fun findByKeycloakId(keycloakId: String): StaffMemberEntity?

    fun findByBusinessId(businessId: UUID): List<StaffMemberEntity>

    @Query("SELECT s FROM StaffMemberEntity s WHERE s.businessId = :businessId AND s.isActive = true")
    fun findActiveByBusinessId(@Param("businessId") businessId: UUID): List<StaffMemberEntity>

    fun findByBusinessIdAndEmail(businessId: UUID, email: String): StaffMemberEntity?

    fun existsByBusinessIdAndEmail(businessId: UUID, email: String): Boolean

    fun existsByKeycloakId(keycloakId: String): Boolean

    @Modifying
    @Transactional
    @Query("UPDATE StaffMemberEntity s SET s.isActive = false, s.updatedAt = :updatedAt WHERE s.id = :id")
    fun deactivateById(@Param("id") id: UUID, @Param("updatedAt") updatedAt: java.time.LocalDateTime)
}