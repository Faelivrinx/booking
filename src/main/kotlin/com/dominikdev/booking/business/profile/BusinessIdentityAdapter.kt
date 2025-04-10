package com.dominikdev.booking.business.profile

import com.dominikdev.booking.business.identity.BusinessIdentityFacade
import com.dominikdev.booking.business.identity.CreateBusinessIdentityCommand
import org.springframework.stereotype.Service
import java.util.*

@Service
class BusinessIdentityAdapter(private val businessIdentityFacade: BusinessIdentityFacade) {

    fun createBusinessIdentity(
        businessId: UUID,
        name: String,
        email: String,
        phoneNumber: String?,
        initialPassword: String
    ): UUID {
        val command = CreateBusinessIdentityCommand(
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            initialPassword = initialPassword,
            businessId = businessId
        )

        val businessIdentity = businessIdentityFacade.createBusinessIdentity(command)
        return businessIdentity.id
    }
}