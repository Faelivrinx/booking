package com.dominikdev.booking.offer.infrastructure.persistance

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

internal interface SpringBusinessRepository : CrudRepository<BusinessEntity, UUID> {

    fun findByOwnerId(ownerId: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE is_active = true")
    fun findAllActive(): List<BusinessEntity>

    fun existsByOwnerId(ownerId: String): Boolean

    @Query("UPDATE businesses SET is_active = false, updated_at = :updatedAt WHERE id = :id")
    fun deactivateById(@Param("id") id: UUID, @Param("updatedAt") updatedAt: java.time.LocalDateTime)
}