package com.dominikdev.booking.appointment.domain.repository

import java.util.*

interface ServiceRepository {
    /**
     * Get the duration of a service in minutes
     */
    fun getServiceDuration(serviceId: UUID): Int?
}