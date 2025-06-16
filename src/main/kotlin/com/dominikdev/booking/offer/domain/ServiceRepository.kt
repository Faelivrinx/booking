package com.dominikdev.booking.offer.domain

import java.util.*

interface ServiceRepository {
    fun save(service: Service): Service
    fun findById(id: UUID): Service?
    fun findByBusinessId(businessId: UUID): List<Service>
    fun findActiveByBusinessId(businessId: UUID): List<Service>
    fun findByBusinessIdAndName(businessId: UUID, name: String): Service?
    fun existsByBusinessIdAndName(businessId: UUID, name: String): Boolean
    // Hard delete - services can be removed completely
    fun delete(id: UUID)
}