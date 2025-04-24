package com.dominikdev.booking.availability.infrastructure.adapter

import java.math.BigDecimal
import java.util.UUID

interface ServiceInfoAdapter {
    fun getServiceDuration(serviceId: UUID): Int?
    fun getServiceName(serviceId: UUID): String?
    fun getServicePrice(serviceId: UUID): BigDecimal?
}