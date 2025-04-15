package com.dominikdev.booking.appointment.domain.services

import java.util.*

interface ServiceProvider {
    fun getServiceById(serviceId: UUID): ServiceInfo?
}