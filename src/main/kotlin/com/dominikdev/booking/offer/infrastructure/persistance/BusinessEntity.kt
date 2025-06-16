package com.dominikdev.booking.offer.infrastructure.persistance

import com.dominikdev.booking.offer.domain.Address
import com.dominikdev.booking.offer.domain.Business
import jakarta.persistence.Column
import jakarta.persistence.Table
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.*

@Table(name = "businesses")
internal data class BusinessEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String,
    val description: String? = null,
    val street: String,
    val city: String,
    val state: String,

    @Column(name = "postal_code")
    val postalCode: String,

    @Column(name ="owner_id")
    val ownerId: String,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun toDomain(): Business {
        return Business(
            id = id,
            name = name,
            description = description,
            address = Address(
                street = street,
                city = city,
                state = state,
                postalCode = postalCode
            ),
            ownerId = ownerId,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(business: Business): BusinessEntity {
            return BusinessEntity(
                id = business.id,
                name = business.name,
                description = business.description,
                street = business.address.street,
                city = business.address.city,
                state = business.address.state,
                postalCode = business.address.postalCode,
                ownerId = business.ownerId,
                isActive = business.isActive,
                createdAt = business.createdAt,
                updatedAt = business.updatedAt
            )
        }
    }
}