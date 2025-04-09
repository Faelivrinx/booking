package com.dominikdev.booking.business.account.application.port.out

interface UserManagementPort {

    fun createBusinessUser(email: String, name: String, phone: String?, password: String, businessId: String): String

    fun deleteUser(userId: String)
}