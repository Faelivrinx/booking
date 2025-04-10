package com.dominikdev.booking.business.profile

import java.util.*

data class BusinessProfileId(val value: UUID)  {
    companion object {
        fun generate(): BusinessProfileId = BusinessProfileId(UUID.randomUUID())
        fun from(id: String): BusinessProfileId = BusinessProfileId(UUID.fromString(id))
        fun from(id: UUID): BusinessProfileId = BusinessProfileId(id)
    }

    override fun toString(): String = value.toString()
}