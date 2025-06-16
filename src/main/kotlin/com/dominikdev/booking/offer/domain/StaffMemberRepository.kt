package com.dominikdev.booking.offer.domain

import java.util.*

interface StaffMemberRepository {
    fun save(staffMember: StaffMember): StaffMember
    fun findById(id: UUID): StaffMember?
    fun findByKeycloakId(keycloakId: String): StaffMember?
    fun findByBusinessId(businessId: UUID): List<StaffMember>
    fun findActiveByBusinessId(businessId: UUID): List<StaffMember>
    fun findByBusinessIdAndEmail(businessId: UUID, email: String): StaffMember?
    fun existsByBusinessIdAndEmail(businessId: UUID, email: String): Boolean
    fun existsByKeycloakId(keycloakId: String): Boolean
    // Soft delete - set isActive = false (staff identities preserved)
    fun deactivate(id: UUID)
}