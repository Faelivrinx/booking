package com.dominikdev.booking.offer.infrastructure.persistance.domain


import com.dominikdev.booking.offer.domain.Service
import com.dominikdev.booking.offer.domain.ServiceRepository
import com.dominikdev.booking.offer.infrastructure.persistance.ServiceEntity
import com.dominikdev.booking.offer.infrastructure.persistance.SpringServiceRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal class JdbcServiceRepository(
    private val springRepository: SpringServiceRepository
) : ServiceRepository {

    override fun save(service: Service): Service {
        val entity = ServiceEntity.fromDomain(service)
        val saved = springRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): Service? {
        return springRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByBusinessId(businessId: UUID): List<Service> {
        return springRepository.findByBusinessId(businessId)
            .map { it.toDomain() }
    }

    override fun findActiveByBusinessId(businessId: UUID): List<Service> {
        return springRepository.findActiveByBusinessId(businessId)
            .map { it.toDomain() }
    }

    override fun findByBusinessIdAndName(businessId: UUID, name: String): Service? {
        return springRepository.findByBusinessIdAndName(businessId, name)?.toDomain()
    }

    override fun existsByBusinessIdAndName(businessId: UUID, name: String): Boolean {
        return springRepository.existsByBusinessIdAndName(businessId, name)
    }

    override fun delete(id: UUID) {
        springRepository.deleteById(id)
    }
}