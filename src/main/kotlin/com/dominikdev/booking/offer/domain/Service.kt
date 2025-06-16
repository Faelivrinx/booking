package com.dominikdev.booking.offer.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Service(
    val id: UUID,
    val businessId: UUID,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val price: BigDecimal,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    init {
        require(name.isNotBlank()) { "Service name cannot be blank" }
        require(durationMinutes > 0) { "Duration must be positive" }
        require(price >= BigDecimal.ZERO) { "Price cannot be negative" }
    }

    fun update(
        name: String,
        description: String?,
        durationMinutes: Int,
        price: BigDecimal
    ): Service {
        return this.copy(
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            durationMinutes = durationMinutes,
            price = price,
            updatedAt = LocalDateTime.now()
        )
    }

    fun deactivate(): Service {
        return copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
    }
}