package com.dominikdev.booking.appointment.domain.repository

import com.dominikdev.booking.appointment.domain.model.StaffServiceAllocation
import java.util.*

interface StaffServiceAllocationRepository {
    /**
     * Save or update a staff service allocation
     */
    fun save(allocation: StaffServiceAllocation): StaffServiceAllocation

    /**
     * Find a staff service allocation by ID
     */
    fun findById(id: UUID): StaffServiceAllocation?

    /**
     * Find a specific staff-service allocation
     */
    fun findByStaffIdAndServiceId(staffId: UUID, serviceId: UUID): StaffServiceAllocation?

    /**
     * Find all service allocations for a staff member
     */
    fun findByStaffId(staffId: UUID): List<StaffServiceAllocation>

    /**
     * Find all staff allocated to a service
     */
    fun findByServiceId(serviceId: UUID): List<StaffServiceAllocation>

    /**
     * Check if a staff member can perform a specific service
     */
    fun canStaffPerformService(staffId: UUID, serviceId: UUID): Boolean

    /**
     * Get all services a staff member can perform
     */
    fun getServicesForStaff(staffId: UUID): List<UUID>

    /**
     * Get all staff that can perform a service
     */
    fun getStaffForService(serviceId: UUID): List<UUID>

    /**
     * Delete a staff service allocation
     */
    fun deleteById(id: UUID)
}