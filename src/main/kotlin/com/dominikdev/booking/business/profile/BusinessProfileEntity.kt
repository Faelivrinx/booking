package com.dominikdev.booking.business.profile

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "businesses_profile")
data class BusinessProfileEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(length = 1000)
    var description: String?,

    @Embedded
    var address: Address,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun update(name: String, description: String?, address: Address) {
        this.name = name
        this.description = description
        this.address = address
        this.updatedAt = LocalDateTime.now()
    }

    companion object {
        fun create(
            name: String,
            description: String?,
            address: Address,
            id: BusinessProfileId = BusinessProfileId.generate()
        ): BusinessProfileEntity {
            val now = LocalDateTime.now()
            return BusinessProfileEntity(
                id = id.value,
                name = name,
                description = description,
                address = address,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}