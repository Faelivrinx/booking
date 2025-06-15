package com.dominikdev.booking.identity.domain

import com.dominikdev.booking.shared.domain.DomainException
import java.time.LocalDateTime
import java.util.*

/**
 * Domain entity representing user profile information from Keycloak
 * This is now a read-only representation of Keycloak user data
 */
data class UserProfile(
    val keycloakId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val businessId: UUID?,
    val isActive: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    fun hasRole(role: UserRole): Boolean = this.role == role

    fun belongsToBusiness(businessId: UUID): Boolean = this.businessId == businessId

    fun getFullName(): String = "$firstName $lastName"
}

enum class UserRole {
    BUSINESS_OWNER, EMPLOYEE, CLIENT, ADMIN
}

enum class Permission {
    MANAGE_BUSINESS, MANAGE_EMPLOYEES, MANAGE_SERVICES,
    MANAGE_SCHEDULE, VIEW_RESERVATIONS, MANAGE_RESERVATIONS,
    VIEW_OWN_SCHEDULE, REQUEST_SCHEDULE_CHANGES
}