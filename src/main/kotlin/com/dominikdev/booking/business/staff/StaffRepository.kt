package com.dominikdev.booking.business.staff


import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StaffRepository : JpaRepository<StaffMember, UUID> {
    fun findAllByBusinessId(businessId: UUID): List<StaffMember>
    fun findByBusinessIdAndEmail(businessId: UUID, email: String): StaffMember?
    fun findByEmail(email: String): StaffMember?
    fun findByKeycloakId(keycloakId: String): StaffMember?
    fun existsByEmail(email: String): Boolean
}