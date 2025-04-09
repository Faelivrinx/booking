package com.dominikdev.booking.business.identity

import com.dominikdev.booking.shared.values.Email
import com.dominikdev.booking.shared.values.Name
import com.dominikdev.booking.shared.values.PhoneNumber
import java.time.LocalDateTime
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "businesses_identity")
class BusinessIdentity(
    @Id
    val id: UUID,

    @Column(name = "keycloak_id", nullable = false, unique = true)
    var keycloakId: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "phone_number")
    var phoneNumber: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime
) {
    fun updateBusinessInfo(
        name: Name,
        email: Email,
        phoneNumber: PhoneNumber?,
    ) {
        this.name = name.value
        this.email = email.value
        this.phoneNumber = phoneNumber?.value
        this.updatedAt = LocalDateTime.now()
    }

    companion object {
        fun create(
            keycloakId: String,
            name: Name,
            email: Email,
            phoneNumber: PhoneNumber?,
            id: BusinessIdentityId = BusinessIdentityId.generate()
        ): BusinessIdentity {

            val businessIdentity = BusinessIdentity(
                id = id.value,
                keycloakId = keycloakId,
                name = name.value,
                email = email.value,
                phoneNumber = phoneNumber?.value,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            return businessIdentity
        }

        fun reconstitute(
            id: BusinessIdentityId,
            keycloakId: String,
            name: String,
            email: String,
            phoneNumber: String?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): BusinessIdentity {
            return BusinessIdentity(
                id = id.value,
                keycloakId = keycloakId,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}