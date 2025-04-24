package com.dominikdev.booking.business.staff

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "staff_service_associations",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_staff_service",
            columnNames = ["staff_id", "service_id"]
        )
    ]
)
class StaffServiceAssociation(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(staffId: UUID, serviceId: UUID): StaffServiceAssociation {
            return StaffServiceAssociation(
                staffId = staffId,
                serviceId = serviceId
            )
        }
    }
}