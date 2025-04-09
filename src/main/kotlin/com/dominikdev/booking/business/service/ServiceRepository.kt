package com.dominikdev.booking.business.service

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceRepository : JpaRepository<ServiceEntity, UUID> {
    fun findAllByBusinessId(businessId: UUID): List<ServiceEntity>
    fun findByBusinessIdAndName(businessId: UUID, name: String): ServiceEntity?
    fun existsByBusinessIdAndName(businessId: UUID, name: String): Boolean
}