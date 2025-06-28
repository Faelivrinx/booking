package com.dominikdev.booking.offer.infrastructure.persistance.domain

import com.dominikdev.booking.offer.domain.StaffServiceAssignment
import com.dominikdev.booking.offer.domain.ServiceAssignmentRepository
import com.dominikdev.booking.offer.infrastructure.persistance.SpringStaffServiceAssignmentRepository
import com.dominikdev.booking.offer.infrastructure.persistance.StaffServiceAssignmentEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class JdbcServiceAssignmentRepository(
    private val springRepository: SpringStaffServiceAssignmentRepository
) : ServiceAssignmentRepository {

    override fun save(assignment: StaffServiceAssignment): StaffServiceAssignment {
        val entity = StaffServiceAssignmentEntity.fromDomain(assignment)
        val saved = springRepository.save(entity)
        return saved.toDomain()
    }

    override fun findByStaffId(staffId: UUID): List<StaffServiceAssignment> {
        return springRepository.findByStaffId(staffId)
            .map { it.toDomain() }
    }

    override fun findByServiceId(serviceId: UUID): List<StaffServiceAssignment> {
        return springRepository.findByServiceId(serviceId)
            .map { it.toDomain() }
    }

    override fun findByBusinessId(businessId: UUID): List<StaffServiceAssignment> {
        return springRepository.findByBusinessId(businessId)
            .map { it.toDomain() }
    }

    override fun findByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): StaffServiceAssignment? {
        return springRepository.findByStaffIdAndServiceId(staffId, serviceId)?.toDomain()
    }

    override fun existsByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): Boolean {
        return springRepository.existsByStaffIdAndServiceId(staffId, serviceId)
    }

    override fun deleteByStaffIdAndServiceId(staffId: UUID, serviceId: UUID) {
        springRepository.deleteByStaffIdAndServiceId(staffId, serviceId)
    }

    override fun deleteByStaffId(staffId: UUID) {
        springRepository.deleteAllByStaffId(staffId)
    }

    override fun deleteByServiceId(serviceId: UUID) {
        springRepository.deleteByServiceId(serviceId)
    }

    override fun replaceStaffServices(staffId: UUID, serviceIds: List<UUID>, businessId: UUID) {
        // First, remove all existing assignments for this staff member
        springRepository.deleteAllByStaffId(staffId)

        // Then add new assignments
        val now = LocalDateTime.now()
        serviceIds.forEach { serviceId ->
            val assignment = StaffServiceAssignment(
                staffId = staffId,
                serviceId = serviceId,
                businessId = businessId,
                assignedAt = now
            )
            save(assignment)
        }
    }
}