package com.dominikdev.booking.business.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "business_staff_members")
class StaffMember(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "business_id", nullable = false)
    val businessId: UUID,

    @Column(name = "keycloak_id", unique = true)
    var keycloakId: String? = null,

    @Column(nullable = false)
    private var firstName: String,

    @Column(nullable = false)
    private var lastName: String,

    @Column(nullable = false, unique = true)
    private var email: String,

    @Column(name = "phone_number")
    private var phoneNumber: String?,

    @Column(name = "job_title")
    private var jobTitle: String?,

    @Column(nullable = false)
    private var active: Boolean = false,

    @Column(name = "activated_at")
    var activatedAt: LocalDateTime? = null,

    @Column(name = "invitation_sent", nullable = false)
    var invitationSent: Boolean = false,

    @Column(name = "invitation_date")
    var invitationDate: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun create(
            businessId: UUID,
            firstName: String,
            lastName: String,
            email: String,
            phoneNumber: String?,
            jobTitle: String?
        ): StaffMember {
            val staffMember = StaffMember(
                businessId = businessId,
                firstName = firstName,
                lastName = lastName,
                email = email.lowercase().trim(),
                phoneNumber = phoneNumber,
                jobTitle = jobTitle,
                active = false
            )
            return staffMember
        }
    }

    fun activate(keycloakId: String) {
        if (this.active) {
            throw StaffDomainException("Staff member is already activated")
        }

        this.active = true
        this.keycloakId = keycloakId
        this.activatedAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    fun deactivate() {
        if (!this.active) {
            throw StaffDomainException("Staff member is already inactive")
        }

        this.active = false
        this.updatedAt = LocalDateTime.now()
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        jobTitle: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.phoneNumber = phoneNumber
        this.jobTitle = jobTitle
        this.updatedAt = LocalDateTime.now()

    }

    fun recordInvitationSent() {
        this.invitationSent = true
        this.invitationDate = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    // Getters
    fun getFirstName(): String = firstName
    fun getLastName(): String = lastName
    fun getEmail(): String = email
    fun getPhoneNumber(): String? = phoneNumber
    fun getJobTitle(): String? = jobTitle
    fun isActive(): Boolean = active
    fun getFullName(): String = "$firstName $lastName"

}