package com.dominikdev.booking.identity.infrastructure
import com.dominikdev.booking.identity.domain.UserProfile
import com.dominikdev.booking.identity.domain.UserProfileRepository
import com.dominikdev.booking.identity.domain.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserProfileJpaRepository : JpaRepository<UserProfileEntity, UUID> {
    fun findByKeycloakId(keycloakId: String): UserProfileEntity?
    fun findByEmail(email: String): UserProfileEntity?
    fun findByBusinessId(businessId: UUID): List<UserProfileEntity>
    fun findByRole(role: UserRole): List<UserProfileEntity>
    fun existsByEmail(email: String): Boolean
}

@Repository
class UserProfileRepositoryImpl(
    private val jpaRepository: UserProfileJpaRepository
) : UserProfileRepository {

    override fun save(userProfile: UserProfile): UserProfile {
        val entity = mapToEntity(userProfile)
        val savedEntity = jpaRepository.save(entity)
        return mapToDomain(savedEntity)
    }

    override fun findById(id: UUID): UserProfile? {
        return jpaRepository.findById(id).map { mapToDomain(it) }.orElse(null)
    }

    override fun findByKeycloakId(keycloakId: String): UserProfile? {
        return jpaRepository.findByKeycloakId(keycloakId)?.let { mapToDomain(it) }
    }

    override fun findByEmail(email: String): UserProfile? {
        return jpaRepository.findByEmail(email)?.let { mapToDomain(it) }
    }

    override fun findByBusinessId(businessId: UUID): List<UserProfile> {
        return jpaRepository.findByBusinessId(businessId).map { mapToDomain(it) }
    }

    override fun findByRole(role: UserRole): List<UserProfile> {
        return jpaRepository.findByRole(role).map { mapToDomain(it) }
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }

    override fun delete(id: UUID) {
        jpaRepository.deleteById(id)
    }

    private fun mapToEntity(userProfile: UserProfile): UserProfileEntity {
        return UserProfileEntity(
            id = userProfile.id,
            keycloakId = userProfile.keycloakId,
            email = userProfile.email,
            firstName = userProfile.firstName,
            lastName = userProfile.lastName,
            phoneNumber = userProfile.phoneNumber,
            role = userProfile.role,
            businessId = userProfile.businessId,
            isActive = userProfile.isActive,
            createdAt = userProfile.createdAt,
            updatedAt = userProfile.updatedAt
        )
    }

    private fun mapToDomain(entity: UserProfileEntity): UserProfile {
        return UserProfile(
            id = entity.id,
            keycloakId = entity.keycloakId,
            email = entity.email,
            firstName = entity.firstName,
            lastName = entity.lastName,
            phoneNumber = entity.phoneNumber,
            role = entity.role,
            businessId = entity.businessId,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}