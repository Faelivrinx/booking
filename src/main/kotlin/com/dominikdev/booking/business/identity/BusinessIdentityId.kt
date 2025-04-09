package com.dominikdev.booking.business.identity

import java.util.UUID

data class BusinessIdentityId(val value: UUID) {
    companion object {
        fun generate(): BusinessIdentityId = BusinessIdentityId(UUID.randomUUID())
        fun from(id: String): BusinessIdentityId = BusinessIdentityId(UUID.fromString(id))
        fun from(id: UUID): BusinessIdentityId = BusinessIdentityId(id)
    }

    override fun toString(): String = value.toString()
}