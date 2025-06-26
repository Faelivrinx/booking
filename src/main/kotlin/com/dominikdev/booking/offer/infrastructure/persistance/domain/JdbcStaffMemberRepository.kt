package com.dominikdev.booking.offer.infrastructure.persistance.domain

import com.dominikdev.booking.offer.domain.StaffMember
import com.dominikdev.booking.offer.domain.StaffMemberRepository
import com.dominikdev.booking.offer.infrastructure.persistance.SpringStaffMemberRepository
import com.dominikdev.booking.offer.infrastructure.persistance.StaffMemberEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class JdbcStaffMemberRepository(
    private val springRepository: SpringStaffMemberRepository
) : StaffMemberRepository {

    override fun save(staffMember: StaffMember): StaffMember {
        val entity = StaffMemberEntity.fromDomain(staffMember)
        val saved = springRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): StaffMember? {
        return springRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByKeycloakId(keycloakId: String): StaffMember? {
        return springRepository.findByKeycloakId(keycloakId)?.toDomain()
    }

    override fun findByBusinessId(businessId: UUID): List<StaffMember> {
        return springRepository.findByBusinessId(businessId)
            .map { it.toDomain() }
    }

    override fun findActiveByBusinessId(businessId: UUID): List<StaffMember> {
        return springRepository.findActiveByBusinessId(businessId)
            .map { it.toDomain() }
    }

    override fun findByBusinessIdAndEmail(businessId: UUID, email: String): StaffMember? {
        return springRepository.findByBusinessIdAndEmail(businessId, email)?.toDomain()
    }

    override fun existsByBusinessIdAndEmail(businessId: UUID, email: String): Boolean {
        return springRepository.existsByBusinessIdAndEmail(businessId, email)
    }

    override fun existsByKeycloakId(keycloakId: String): Boolean {
        return springRepository.existsByKeycloakId(keycloakId)
    }

    override fun deactivate(id: UUID) {
        springRepository.deactivateById(id, LocalDateTime.now())
    }
}