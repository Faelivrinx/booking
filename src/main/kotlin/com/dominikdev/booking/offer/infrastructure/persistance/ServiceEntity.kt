package com.dominikdev.booking.offer.infrastructure.persistance

import com.dominikdev.booking.offer.domain.Service
import jakarta.persistence.Column
import jakarta.persistence.Table
import org.springframework.data.annotation.Id
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Table(name = "services")
internal data class ServiceEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "business_id")
    val businessId: UUID,

    val name: String,
    val description: String? = null,

    @Column(name = "duration_minutes")
    val durationMinutes: Int,

    val price: BigDecimal,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun toDomain(): Service {
        return Service(
            id = id,
            businessId = businessId,
            name = name,
            description = description,
            durationMinutes = durationMinutes,
            price = price,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(service: Service): ServiceEntity {
            return ServiceEntity(
                id = service.id,
                businessId = service.businessId,
                name = service.name,
                description = service.description,
                durationMinutes = service.durationMinutes,
                price = service.price,
                isActive = service.isActive,
                createdAt = service.createdAt,
                updatedAt = service.updatedAt
            )
        }
    }
}