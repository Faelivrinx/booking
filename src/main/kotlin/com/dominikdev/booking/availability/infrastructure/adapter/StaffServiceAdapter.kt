package com.dominikdev.booking.availability.infrastructure.adapter

import java.util.*

/**
 * Adapter interface for fetching staff-service associations from
 * the business/staff context
 */
interface StaffServiceAdapter {
    /**
     * Get all services a staff member can perform
     */
    fun getServicesForStaff(staffId: UUID): List<UUID>

    /**
     * Get all staff who can perform a service
     */
    fun getStaffForService(serviceId: UUID): List<UUID>

    /**
     * Check if a staff member can perform a specific service
     */
    fun canStaffPerformService(staffId: UUID, serviceId: UUID): Boolean
}