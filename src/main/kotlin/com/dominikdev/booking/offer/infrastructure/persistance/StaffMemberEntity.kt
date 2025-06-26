package com.dominikdev.booking.offer.infrastructure.persistance

import com.dominikdev.booking.offer.domain.StaffMember
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Table(name = "staff_members")
@Entity
data class StaffMemberEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "business_id")
    val businessId: UUID,

    @Column(name = "keycloak_id")
    val keycloakId: String,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "last_name")
    val lastName: String,

    val email: String,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(name = "job_title")
    val jobTitle: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun toDomain(): StaffMember {
        return StaffMember(
            id = id,
            businessId = businessId,
            keycloakId = keycloakId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            jobTitle = jobTitle,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(staffMember: StaffMember): StaffMemberEntity {
            return StaffMemberEntity(
                id = staffMember.id,
                businessId = staffMember.businessId,
                keycloakId = staffMember.keycloakId,
                firstName = staffMember.firstName,
                lastName = staffMember.lastName,
                email = staffMember.email,
                phoneNumber = staffMember.phoneNumber,
                jobTitle = staffMember.jobTitle,
                isActive = staffMember.isActive,
                createdAt = staffMember.createdAt,
                updatedAt = staffMember.updatedAt
            )
        }
    }
}