package com.dominikdev.booking.offer.infrastructure.persistance.domain

import com.dominikdev.booking.offer.domain.Business
import com.dominikdev.booking.offer.domain.BusinessRepository
import com.dominikdev.booking.offer.infrastructure.persistance.BusinessEntity
import com.dominikdev.booking.offer.infrastructure.persistance.SpringBusinessRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class JdbcBusinessRepository(
    private val springRepository: SpringBusinessRepository
) : BusinessRepository {

    override fun save(business: Business): Business {
        val entity = BusinessEntity.fromDomain(business)
        val saved = springRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): Business? {
        return springRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByOwnerId(ownerId: String): Business? {
        return springRepository.findByOwnerId(ownerId)?.toDomain()
    }

    override fun findAll(): List<Business> {
        return springRepository.findAll().map { it.toDomain() }
    }

    override fun findAllActive(): List<Business> {
        return springRepository.findAllActive().map { it.toDomain() }
    }

    override fun existsById(id: UUID): Boolean {
        return springRepository.existsById(id)
    }

    override fun existsByOwnerId(ownerId: String): Boolean {
        return springRepository.existsByOwnerId(ownerId)
    }

    override fun deactivate(id: UUID) {
        springRepository.deactivateById(id, LocalDateTime.now())
    }
}