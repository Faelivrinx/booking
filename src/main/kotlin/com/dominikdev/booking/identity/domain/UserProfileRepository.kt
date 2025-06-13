package com.dominikdev.booking.identity.domain

import java.util.*

interface UserProfileRepository {
    fun save(userProfile: UserProfile): UserProfile
    fun findById(id: UUID): UserProfile?
    fun findByKeycloakId(keycloakId: String): UserProfile?
    fun findByEmail(email: String): UserProfile?
    fun findByBusinessId(businessId: UUID): List<UserProfile>
    fun findByRole(role: UserRole): List<UserProfile>
    fun existsByEmail(email: String): Boolean
    fun delete(id: UUID)
}