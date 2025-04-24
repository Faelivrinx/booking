package com.dominikdev.booking.availability.domain.repository

import java.math.BigDecimal
import java.util.UUID

interface ServiceRepository {
    fun getServiceDuration(serviceId: UUID): Int?
    fun getServiceName(serviceId: UUID): String?
    fun getServicePrice(serviceId: UUID): BigDecimal?
}