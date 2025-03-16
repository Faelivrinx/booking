package com.dominikdev.booking.business.domain

import java.util.UUID

data class BusinessId(val value: UUID) {
    companion object {
        fun generate(): BusinessId = BusinessId(UUID.randomUUID())
        fun from(id: String): BusinessId = BusinessId(UUID.fromString(id))
        fun from(id: UUID): BusinessId = BusinessId(id)
    }

    override fun toString(): String = value.toString()
}