package com.dominikdev.booking.availability.infrastructure.adapter

import com.dominikdev.booking.business.service.ServiceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class BusinessServiceAdapter(
    private val serviceRepository: ServiceRepository
) : ServiceInfoAdapter {

    override fun getServiceDuration(serviceId: UUID): Int? {
        return serviceRepository.findById(serviceId)
            .map { it.durationMinutes }
            .orElse(null)
    }

    override fun getServiceName(serviceId: UUID): String? {
        return serviceRepository.findById(serviceId)
            .map { it.name }
            .orElse(null)
    }

    override fun getServicePrice(serviceId: UUID): BigDecimal? {
        return serviceRepository.findById(serviceId)
            .map { it.price }
            .orElse(null)
    }
}