package com.dominikdev.booking.offer.infrastructure.persistance

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface SpringServiceRepository : CrudRepository<ServiceEntity, UUID> {

    fun findByBusinessId(businessId: UUID): List<ServiceEntity>

    @Query("SELECT s FROM ServiceEntity s WHERE s.businessId = :businessId AND s.isActive = true")
    fun findActiveByBusinessId(@Param("businessId") businessId: UUID): List<ServiceEntity>

    fun findByBusinessIdAndName(businessId: UUID, name: String): ServiceEntity?

    fun existsByBusinessIdAndName(businessId: UUID, name: String): Boolean
}