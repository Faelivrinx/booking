package com.dominikdev.booking.offer.infrastructure.persistance

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface SpringBusinessRepository : CrudRepository<BusinessEntity, UUID> {

    fun findByOwnerId(ownerId: String): BusinessEntity?

    @Query("SELECT b FROM BusinessEntity b WHERE b.isActive = true")
    fun findAllActive(): List<BusinessEntity>

    fun existsByOwnerId(ownerId: String): Boolean

    @Modifying
    @Transactional
    @Query("UPDATE BusinessEntity b SET b.isActive = false, b.updatedAt = :updatedAt WHERE b.id = :id")
    fun deactivateById(@Param("id") id: UUID, @Param("updatedAt") updatedAt: java.time.LocalDateTime)
}