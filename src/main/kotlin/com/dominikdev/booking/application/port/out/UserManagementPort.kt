package com.dominikdev.booking.application.port.out

interface UserManagementPort {

    fun createBusinessUser(email: String, name: String, phone: String?, password: String): String

    fun deleteUser(userId: String)
}