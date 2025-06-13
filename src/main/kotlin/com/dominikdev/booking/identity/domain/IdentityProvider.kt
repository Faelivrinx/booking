package com.dominikdev.booking.identity.domain

import java.util.*

interface IdentityProvider {
    fun createBusinessOwnerUser(email: String, firstName: String, lastName: String, temporaryPassword: String, businessId: UUID): String
    fun createEmployeeUser(email: String, firstName: String, lastName: String, temporaryPassword: String, businessId: UUID): String
    fun createClientUser(email: String, firstName: String, lastName: String, password: String): String
    fun deactivateUser(keycloakId: String)
    fun updateUserPassword(keycloakId: String, newPassword: String)
    fun sendPasswordResetEmail(email: String)
}