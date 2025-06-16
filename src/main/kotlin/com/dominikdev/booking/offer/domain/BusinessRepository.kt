package com.dominikdev.booking.offer.domain

import java.util.*

interface BusinessRepository {
    fun save(business: Business): Business
    fun findById(id: UUID): Business?
    fun findByOwnerId(ownerId: String): Business?
    fun findAll(): List<Business>
    fun findAllActive(): List<Business>
    fun existsById(id: UUID): Boolean
    fun existsByOwnerId(ownerId: String): Boolean
    // Soft delete - set isActive = false
    fun deactivate(id: UUID)
}