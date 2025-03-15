package com.dominikdev.booking.infrastructure.security

import com.dominikdev.booking.application.port.out.UserManagementPort

class KeycloakUserManagementAdapter() : UserManagementPort {

    override fun createBusinessUser(
        email: String,
        name: String,
        phone: String?,
        password: String
    ): String {
        TODO("Not yet implemented")
    }

    override fun deleteUser(userId: String) {
        TODO("Not yet implemented")
    }
}