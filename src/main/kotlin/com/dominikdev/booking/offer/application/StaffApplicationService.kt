package com.dominikdev.booking.offer.application

import com.dominikdev.booking.identity.CreateEmployeeAccountRequest
import com.dominikdev.booking.identity.IdentityFacade
import com.dominikdev.booking.identity.domain.UserRole
import com.dominikdev.booking.offer.AddStaffMemberRequest
import com.dominikdev.booking.offer.UpdateStaffMemberRequest
import com.dominikdev.booking.offer.domain.*
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

class StaffApplicationService(
    private val staffMemberRepository: StaffMemberRepository,
    private val businessApplicationService: BusinessApplicationService,
    private val identityFacade: IdentityFacade,
    private val staffServiceAssignmentRepository: StaffServiceAssignmentRepository
) {

    private val logger = LoggerFactory.getLogger(StaffApplicationService::class.java)

    @Transactional
    fun addStaffMember(businessId: UUID, request: AddStaffMemberRequest): StaffMember {
        logger.info("Adding staff member '${request.email}' to business: $businessId")

        // Validate business ownership (only business owners can add staff)
        validateBusinessOwnership(businessId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        // Check for duplicate email within business
        if (staffMemberRepository.existsByBusinessIdAndEmail(businessId, request.email.trim())) {
            throw DuplicateStaffMemberException(request.email)
        }

        try {
            // Step 1: Create employee in Identity context
            val temporaryPassword = generateTemporaryPassword()
            val createEmployeeRequest = CreateEmployeeAccountRequest(
                email = request.email.trim(),
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim(),
                phoneNumber = request.phoneNumber?.trim()?.takeIf { it.isNotEmpty() },
                businessId = businessId,
                temporaryPassword = temporaryPassword
            )

            val employeeAccount = identityFacade.createEmployeeAccount(createEmployeeRequest)
            logger.info("Created employee account: ${employeeAccount.keycloakId}")

            // Step 2: Create staff member
            val staffMember = StaffMember(
                id = UUID.randomUUID(),
                businessId = businessId,
                keycloakId = employeeAccount.keycloakId,
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim(),
                email = request.email.trim(),
                phoneNumber = request.phoneNumber?.trim()?.takeIf { it.isNotEmpty() },
                jobTitle = request.jobTitle?.trim()?.takeIf { it.isNotEmpty() },
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            val saved = staffMemberRepository.save(staffMember)
            logger.info("Successfully added staff member: ${saved.id}")
            logger.info("Temporary password for ${request.email}: $temporaryPassword")

            return saved

        } catch (e: Exception) {
            logger.error("Failed to add staff member: ${request.email}", e)
            throw InvalidBusinessOperationException("Failed to add staff member: ${e.message}")
        }
    }

    @Transactional
    fun updateStaffMember(businessId: UUID, staffId: UUID, request: UpdateStaffMemberRequest): StaffMember {
        logger.info("Updating staff member: $staffId")

        // Validate business ownership
        validateBusinessOwnership(businessId)

        // Validate business exists and is active
        businessApplicationService.validateBusinessActive(businessId)

        val existingStaff = staffMemberRepository.findById(staffId)
            ?: throw StaffMemberNotFoundException(staffId)

        // Verify staff belongs to the business
        if (existingStaff.businessId != businessId) {
            throw InvalidBusinessOperationException("Staff member does not belong to business: $businessId")
        }

        if (!existingStaff.isActive) {
            throw InvalidBusinessOperationException("Cannot update deactivated staff member")
        }

        val updatedStaff = existingStaff.updateProfile(
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            phoneNumber = request.phoneNumber?.trim()?.takeIf { it.isNotEmpty() },
            jobTitle = request.jobTitle?.trim()?.takeIf { it.isNotEmpty() }
        )

        val saved = staffMemberRepository.save(updatedStaff)
        logger.info("Successfully updated staff member: $staffId")

        return saved
    }

    @Transactional
    fun deactivateStaffMember(businessId: UUID, staffId: UUID) {
        logger.info("Deactivating staff member: $staffId")

        // Validate business ownership
        validateBusinessOwnership(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        val staffMember = staffMemberRepository.findById(staffId)
            ?: throw StaffMemberNotFoundException(staffId)

        // Verify staff belongs to the business
        if (staffMember.businessId != businessId) {
            throw InvalidBusinessOperationException("Staff member does not belong to business: $businessId")
        }

        if (!staffMember.isActive) {
            logger.warn("Staff member already deactivated: $staffId")
            return
        }

        // Deactivate in Keycloak
        identityFacade.deactivateEmployeeAccount(staffMember.keycloakId)

        // Soft delete staff member
        staffMemberRepository.deactivate(staffId)

        // Remove all service assignments
        staffServiceAssignmentRepository.deleteByStaffId(staffId)

        logger.info("Successfully deactivated staff member: $staffId")
    }

    @Transactional(readOnly = true)
    fun getBusinessStaff(businessId: UUID): List<StaffMember> {
        logger.debug("Retrieving staff for business: $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        return staffMemberRepository.findByBusinessId(businessId)
    }

    @Transactional(readOnly = true)
    fun getActiveBusinessStaff(businessId: UUID): List<StaffMember> {
        logger.debug("Retrieving active staff for business: $businessId")

        // Validate business access
        validateBusinessAccess(businessId)

        // Validate business exists
        businessApplicationService.validateBusinessExists(businessId)

        return staffMemberRepository.findActiveByBusinessId(businessId)
    }

    @Transactional(readOnly = true)
    fun getStaffMember(businessId: UUID, staffId: UUID): StaffMember? {
        logger.debug("Retrieving staff member: $staffId")

        // Validate business access
        validateBusinessAccess(businessId)

        val staffMember = staffMemberRepository.findById(staffId)

        // Verify staff belongs to the business if found
        if (staffMember != null && staffMember.businessId != businessId) {
            return null
        }

        return staffMember
    }

    @Transactional(readOnly = true)
    fun validateStaffMemberExists(staffId: UUID): StaffMember {
        return staffMemberRepository.findById(staffId)
            ?: throw StaffMemberNotFoundException(staffId)
    }

    @Transactional(readOnly = true)
    fun validateStaffMemberActive(staffId: UUID): StaffMember {
        val staffMember = validateStaffMemberExists(staffId)
        if (!staffMember.isActive) {
            throw InvalidBusinessOperationException("Staff member is not active: $staffId")
        }
        return staffMember
    }

    // Methods for other contexts (no security validation needed)
    @Transactional(readOnly = true)
    fun isStaffMemberActive(staffId: UUID): Boolean {
        return staffMemberRepository.findById(staffId)?.isActive ?: false
    }

    @Transactional(readOnly = true)
    fun getStaffMemberBusinessId(staffId: UUID): UUID? {
        return staffMemberRepository.findById(staffId)?.businessId
    }

    private fun validateBusinessOwnership(businessId: UUID) {
        val userAttributes = identityFacade.extractUserAttributes()

        // Admins can modify any business
        if (userAttributes.role == UserRole.ADMIN) {
            return
        }

        // Only business owners can modify staff
        if (userAttributes.role == UserRole.BUSINESS_OWNER) {
            if (userAttributes.businessId != businessId) {
                throw UnauthorizedBusinessAccessException("User does not own business: $businessId")
            }
            return
        }

        throw UnauthorizedBusinessAccessException("Only business owners can modify staff")
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

    private fun generateTemporaryPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..12)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}