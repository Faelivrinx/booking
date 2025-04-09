package com.dominikdev.booking.business.profile.infrastructure.persistance

import com.dominikdev.booking.business.profile.domain.Business
import com.dominikdev.booking.business.profile.domain.BusinessId
import com.dominikdev.booking.business.profile.domain.BusinessRepository
import com.dominikdev.booking.shared.values.Email
import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class DefaultBusinessRepository(
    private val businessJpaRepository: BusinessJpaRepository,
    private val eventPublisher: DomainEventPublisher

) : BusinessRepository {

    @Transactional
    override fun save(business: Business): Business {
        val entity = toEntity(business)
        val savedEntity = businessJpaRepository.save(entity)
        val savedBusiness = toDomain(savedEntity)

        val events = business.getEvents()
        if (events.isNotEmpty()) {
            eventPublisher.publishAll(events)
            business.clearEvents()
        }

        return savedBusiness
    }

    @Transactional(readOnly = true)
    override fun findById(id: BusinessId): Business? {
        return businessJpaRepository.findById(id.value)
            .map { toDomain(it) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findByKeycloakId(keycloakId: String): Business? {
        return businessJpaRepository.findByKeycloakId(keycloakId)
            ?.let { toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findByEmail(email: Email): Business? {
        return businessJpaRepository.findByEmail(email.value)
            ?.let { toDomain(it) }
    }

    private fun toEntity(business: Business): BusinessEntity {
        return BusinessEntity(
            id = business.id.value,
            keycloakId = business.keycloakId,
            name = business.getName().value,
            email = business.getEmail().value,
            phoneNumber = business.getPhoneNumber()?.value,
            createdAt = business.createdAt,
            updatedAt = business.updatedAt
        )
    }

    private fun toDomain(entity: BusinessEntity): Business {
        return Business.reconstitute(
            id = BusinessId.from(entity.id),
            keycloakId = entity.keycloakId,
            name = entity.name,
            email = entity.email,
            phoneNumber = entity.phoneNumber,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}