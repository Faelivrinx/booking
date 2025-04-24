package com.dominikdev.booking.business.staff

import com.dominikdev.booking.business.service.ServiceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StaffServiceAssociationService(
    private val staffServiceRepository: StaffServiceRepository,
    private val staffRepository: StaffRepository,
    private val serviceRepository: ServiceRepository
) {
    /**
     * Get all services a staff member can perform
     */
    @Transactional(readOnly = true)
    fun getServicesForStaff(staffId: UUID): List<UUID> {
        return staffServiceRepository.findServicesByStaffId(staffId)
    }

    /**
     * Get all staff who can perform a service
     */
    @Transactional(readOnly = true)
    fun getStaffForService(serviceId: UUID): List<UUID> {
        return staffServiceRepository.findStaffByServiceId(serviceId)
    }

    /**
     * Check if a staff member can perform a specific service
     */
    @Transactional(readOnly = true)
    fun canStaffPerformService(staffId: UUID, serviceId: UUID): Boolean {
        return staffServiceRepository.existsByStaffIdAndServiceId(staffId, serviceId)
    }

    /**
     * Assign a service to a staff member
     */
    @Transactional
    fun assignServiceToStaff(staffId: UUID, serviceId: UUID) {
        // Validate staff exists
        if (!staffRepository.existsById(staffId)) {
            throw StaffDomainException("Staff member with ID $staffId not found")
        }

        // Validate service exists
        if (!serviceRepository.existsById(serviceId)) {
            throw StaffDomainException("Service with ID $serviceId not found")
        }

        // Check if association already exists
        if (staffServiceRepository.existsByStaffIdAndServiceId(staffId, serviceId)) {
            // Already assigned, nothing to do
            return
        }

        // Create and save the association
        val association = StaffServiceAssociation.create(staffId, serviceId)
        staffServiceRepository.save(association)
    }

    /**
     * Remove a service from a staff member
     */
    @Transactional
    fun removeServiceFromStaff(staffId: UUID, serviceId: UUID) {
        staffServiceRepository.deleteByStaffIdAndServiceId(staffId, serviceId)
    }

    /**
     * Assign multiple services to a staff member
     */
    @Transactional
    fun assignServicesToStaff(staffId: UUID, serviceIds: List<UUID>) {
        // Validate staff exists
        if (!staffRepository.existsById(staffId)) {
            throw StaffDomainException("Staff member with ID $staffId not found")
        }

        // Get currently assigned services
        val existingServices = staffServiceRepository.findServicesByStaffId(staffId).toSet()

        // Filter out services that are already assigned
        val newServices = serviceIds.filter { !existingServices.contains(it) }

        // Create and save new associations
        val associations = newServices.map { serviceId ->
            // Validate each service exists
            if (!serviceRepository.existsById(serviceId)) {
                throw StaffDomainException("Service with ID $serviceId not found")
            }

            StaffServiceAssociation.create(staffId, serviceId)
        }

        if (associations.isNotEmpty()) {
            staffServiceRepository.saveAll(associations)
        }
    }

    /**
     * Set the exact services for a staff member (add missing, remove extras)
     */
    @Transactional
    fun setStaffServices(staffId: UUID, serviceIds: List<UUID>) {
        // Validate staff exists
        if (!staffRepository.existsById(staffId)) {
            throw StaffDomainException("Staff member with ID $staffId not found")
        }

        // Get currently assigned services
        val existingServices = staffServiceRepository.findServicesByStaffId(staffId).toSet()
        val targetServices = serviceIds.toSet()

        // Services to add
        val servicesToAdd = targetServices.minus(existingServices)

        // Services to remove
        val servicesToRemove = existingServices.minus(targetServices)

        // Remove services
        for (serviceId in servicesToRemove) {
            staffServiceRepository.deleteByStaffIdAndServiceId(staffId, serviceId)
        }

        // Add new services
        val associations = servicesToAdd.map { serviceId ->
            // Validate each service exists
            if (!serviceRepository.existsById(serviceId)) {
                throw StaffDomainException("Service with ID $serviceId not found")
            }

            StaffServiceAssociation.create(staffId, serviceId)
        }

        if (associations.isNotEmpty()) {
            staffServiceRepository.saveAll(associations)
        }
    }

    /**
     * Count how many services a staff member can perform
     */
    @Transactional(readOnly = true)
    fun countStaffServices(staffId: UUID): Int {
        return staffServiceRepository.countServicesByStaffId(staffId)
    }
}