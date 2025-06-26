package com.dominikdev.booking.offer.infrastructure.persistance

import com.dominikdev.booking.offer.domain.StaffServiceAssignment
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@Table(name ="staff_service_assignments")
@Entity
@IdClass(StaffServiceAssignmentId::class)
data class StaffServiceAssignmentEntity(

    @Id
    @Column(name ="staff_id")
    val staffId: UUID,

    @Column(name ="service_id")
    val serviceId: UUID,

    @Column(name ="business_id")
    val businessId: UUID,

    @Column(name ="assigned_at")
    val assignedAt: LocalDateTime = LocalDateTime.now()
) {

    fun toDomain(): StaffServiceAssignment {
        return StaffServiceAssignment(
            staffId = staffId,
            serviceId = serviceId,
            businessId = businessId,
            assignedAt = assignedAt
        )
    }

    companion object {
        fun fromDomain(assignment: StaffServiceAssignment): StaffServiceAssignmentEntity {
            return StaffServiceAssignmentEntity(
                staffId = assignment.staffId,
                serviceId = assignment.serviceId,
                businessId = assignment.businessId,
                assignedAt = assignment.assignedAt
            )
        }
    }
}

@Embeddable
data class StaffServiceAssignmentId(
    val staffId: UUID,
    val serviceId: UUID
) : Serializable {

}