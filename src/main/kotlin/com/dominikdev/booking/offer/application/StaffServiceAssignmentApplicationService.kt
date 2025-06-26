package com.dominikdev.booking.offer.application

import com.dominikdev.booking.identity.IdentityFacade
import com.dominikdev.booking.identity.domain.UserRole
import com.dominikdev.booking.offer.domain.*
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.util.*

open class StaffServiceAssignmentApplicationService(
    private val staffServiceAssignmentRepository: StaffServiceAssignmentRepository,
    private val businessApplicationService: BusinessApplicationService,
    private val serviceApplicationService: ServiceApplicationService,
    private val staffApplicationService: StaffApplicationService,
    private val identityFacade: IdentityFacade
) {

    private val logger = LoggerFactory.getLogger(StaffServiceAssignmentApplicationService::class.java)

    @Transactional
    fun assignServiceToStaff(businessId: UUID, staffId: UUID, serviceId: UUID) {
        logger.info("Assigning service $serviceId to staff $staffId in business $businessId")

        // Validate business access and assignment permissions
        validateAssignmentPermissions(businessId, staffId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        // Validate staff member exists, is active, and belongs to business
        val staffMember = staffApplicationService.validateStaffMemberActive(staffId)
        if (staffMember.businessId != businessId) {
            throw InvalidBusinessOperationException("Staff member does not belong to business: $businessId")
        }

        // Validate service exists, is active, and belongs to business
        val service = serviceApplicationService.validateServiceActive(serviceId)
        if (service.businessId != businessId) {
            throw InvalidBusinessOperationException("Service $serviceId does not belong to business: $businessId")
        }

        // Check if assignment already exists
        if (staffServiceAssignmentRepository.existsByStaffIdAndServiceId(staffId, serviceId)) {
            logger.info("Service $serviceId already assigned to staff $staffId")
            return
        }

        // Create assignment
        val assignment = StaffServiceAssignment(
            staffId = staffId,
            serviceId = serviceId,
            businessId = businessId
        )

        staffServiceAssignmentRepository.save(assignment)
        logger.info("Successfully assigned service $serviceId to staff $staffId")
    }

    @Transactional
    fun unassignServiceFromStaff(businessId: UUID, staffId: UUID, serviceId: UUID) {
        logger.info("Unassigning service $serviceId from staff $staffId in business $businessId")

        // Validate business access and assignment permissions
        validateAssignmentPermissions(businessId, staffId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        // Validate staff member belongs to business
        val staffMember = staffApplicationService.validateStaffMemberExists(staffId)
        if (staffMember.businessId != businessId) {
            throw InvalidBusinessOperationException("Staff member does not belong to business: $businessId")
        }

        // Validate service belongs to business
        val service = serviceApplicationService.validateServiceExists(serviceId)
        if (service.businessId != businessId) {
            throw InvalidBusinessOperationException("Service does not belong to business: $businessId")
        }

        // Remove assignment if it exists
        if (staffServiceAssignmentRepository.existsByStaffIdAndServiceId(staffId, serviceId)) {
            staffServiceAssignmentRepository.deleteByStaffIdAndServiceId(staffId, serviceId)
            logger.info("Successfully unassigned service $serviceId from staff $staffId")
        } else {
            logger.info("Service $serviceId was not assigned to staff $staffId")
        }
    }

    @Transactional
    fun setStaffServices(businessId: UUID, staffId: UUID, serviceIds: List<UUID>) {
        logger.info("Setting ${serviceIds.size} services for staff $staffId in business $businessId")

        // Validate business access and assignment permissions
        validateAssignmentPermissions(businessId, staffId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        // Validate staff member exists, is active, and belongs to business
        val staffMember = staffApplicationService.validateStaffMemberActive(staffId)
        if (staffMember.businessId != businessId) {
            throw InvalidBusinessOperationException("Staff member does not belong to business: $businessId")
        }

        // Validate all services exist, are active, and belong to business
        serviceIds.forEach { serviceId ->
            val service = serviceApplicationService.validateServiceActive(serviceId)
            if (service.businessId != businessId) {
                throw InvalidBusinessOperationException("Service $serviceId does not belong to business: $businessId")
            }
        }

        // Remove duplicates
        val uniqueServiceIds = serviceIds.distinct()

        // Use atomic replacement
        staffServiceAssignmentRepository.replaceStaffServices(staffId, uniqueServiceIds, businessId)
        logger.info("Successfully set ${uniqueServiceIds.size} services for staff $staffId")
    }

    @Transactional(readOnly = true)
    fun getStaffServices(businessId: UUID, staffId: UUID): List<com.dominikdev.booking.offer.domain.Service> {
        logger.debug("Retrieving services for staff $staffId in business $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        // Validate staff member belongs to business
        val staffMember = staffApplicationService.validateStaffMemberExists(staffId)
        if (staffMember.businessId != businessId) {
            throw InvalidBusinessOperationException("Staff member does not belong to business: $businessId")
        }

        val assignments = staffServiceAssignmentRepository.findByStaffId(staffId)
        val serviceIds = assignments.map { it.serviceId }

        // Get services and filter out any that might have been deleted
        return serviceIds.mapNotNull { serviceId ->
            serviceApplicationService.getService(businessId, serviceId)
        }
    }

    @Transactional(readOnly = true)
    fun getServiceStaff(businessId: UUID, serviceId: UUID): List<StaffMember> {
        logger.debug("Retrieving staff for service $serviceId in business $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        // Validate service belongs to business
        val service = serviceApplicationService.validateServiceExists(serviceId)
        if (service.businessId != businessId) {
            throw InvalidBusinessOperationException("Service does not belong to business: $businessId")
        }

        val assignments = staffServiceAssignmentRepository.findByServiceId(serviceId)
        val staffIds = assignments.map { it.staffId }

        // Get staff members and filter out any that might have been deleted
        return staffIds.mapNotNull { staffId ->
            staffApplicationService.getStaffMember(businessId, staffId)
        }
    }

    @Transactional(readOnly = true)
    fun getActiveServiceStaff(businessId: UUID, serviceId: UUID): List<StaffMember> {
        logger.debug("Retrieving active staff for service $serviceId in business $businessId")

        return getServiceStaff(businessId, serviceId)
            .filter { it.isActive }
    }

    @Transactional(readOnly = true)
    fun getActiveStaffServices(businessId: UUID, staffId: UUID): List<com.dominikdev.booking.offer.domain.Service> {
        logger.debug("Retrieving active services for staff $staffId in business $businessId")

        return getStaffServices(businessId, staffId)
            .filter { it.isActive }
    }

    @Transactional(readOnly = true)
    fun isStaffAssignedToService(staffId: UUID, serviceId: UUID): Boolean {
        return staffServiceAssignmentRepository.existsByStaffIdAndServiceId(staffId, serviceId)
    }

    @Transactional(readOnly = true)
    fun getBusinessAssignments(businessId: UUID): List<StaffServiceAssignment> {
        logger.debug("Retrieving all assignments for business $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        return staffServiceAssignmentRepository.findByBusinessId(businessId)
    }

    private fun validateAssignmentPermissions(businessId: UUID, staffId: UUID) {
        val userAttributes = identityFacade.extractUserAttributes()

        // Admins can modify any assignments
        if (userAttributes.role == UserRole.ADMIN) {
            return
        }

        // Business owners can modify assignments in their business
        if (userAttributes.role == UserRole.BUSINESS_OWNER) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not own business: $businessId")
            }
            return
        }

        // Staff members can only modify their own assignments
        if (userAttributes.role == UserRole.EMPLOYEE) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not belong to business: $businessId")
            }

            // Check if current user is the staff member being modified
            val staffMember = staffApplicationService.validateStaffMemberExists(staffId)
            if (staffMember.keycloakId != userAttributes.keycloakId) {
                throw UnauthorizedBusinessAccessException("Staff members can only modify their own service assignments")
            }
            return
        }

        throw UnauthorizedBusinessAccessException("Insufficient permissions for service assignments")
    }

    private fun validateBusinessAccess(businessId: UUID) {
        val userAttributes = identityFacade.extractUserAttributes()

        // Admins can access any business
        if (userAttributes.role == UserRole.ADMIN) {
            return
        }

        // Business owners and staff can access their business
        if (userAttributes.role == UserRole.BUSINESS_OWNER || userAttributes.role == UserRole.EMPLOYEE) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not have access to business: $businessId")
            }
            return
        }

        throw UnauthorizedBusinessAccessException("Insufficient permissions for business access")
    }
}