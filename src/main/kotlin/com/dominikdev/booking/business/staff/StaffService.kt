package com.dominikdev.booking.business.staff

import com.dominikdev.booking.shared.infrastructure.event.DomainEventPublisher
import com.dominikdev.booking.shared.infrastructure.identity.IdentityManagementService
import com.dominikdev.booking.shared.infrastructure.identity.UserRole
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StaffService(
    private val staffRepository: StaffRepository,
    private val identityService: IdentityManagementService,
    private val eventPublisher: DomainEventPublisher,
    private val staffNotificationService: StaffNotificationService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Creates a new staff member for a business
     */
    @Transactional
    fun createStaffMember(request: CreateStaffRequest): StaffMemberDTO {
        val businessId = request.businessId

        // Check if email is already in use
        if (staffRepository.existsByEmail(request.email)) {
            throw StaffDomainException("Email ${request.email} is already in use")
        }

        // Create the staff member entity
        val staffMember = StaffMember.create(
            businessId = businessId,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phoneNumber = request.phoneNumber,
            jobTitle = request.jobTitle
        )

        // Save the staff member
        val savedStaffMember = staffRepository.save(staffMember)

        // Create a temporary password for the staff member
        val temporaryPassword = generateTemporaryPassword()

        try {
            // Create the Keycloak user and get the ID
            identityService.createStaffMember(
                email = request.email,
                name = "${request.firstName} ${request.lastName}",
                phone = request.phoneNumber,
                password = temporaryPassword,
                businessId = businessId
            )

            // Send invitation email with temporary password
            staffNotificationService.sendInvitation(
                email = request.email,
                name = "${request.firstName} ${request.lastName}",
                temporaryPassword = temporaryPassword,
                businessName = request.businessName
            )

            // Mark invitation as sent
            savedStaffMember.recordInvitationSent()
            staffRepository.save(savedStaffMember)

            return mapToDTO(savedStaffMember)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create staff member in Keycloak: ${e.message}" }
            throw StaffDomainException("Failed to create staff member account: ${e.message}", e)
        }
    }

    /**
     * Gets all staff members for a business
     */
    @Transactional(readOnly = true)
    fun getStaffMembers(businessId: UUID): List<StaffMemberDTO> {
        return staffRepository.findAllByBusinessId(businessId)
            .map { mapToDTO(it) }
    }

    /**
     * Gets a staff member by ID
     */
    @Transactional(readOnly = true)
    fun getStaffMemberById(businessId: UUID, staffId: UUID): StaffMemberDTO {
        val staffMember = staffRepository.findById(staffId)
            .orElseThrow { StaffDomainException("Staff member not found with ID: $staffId") }

        // Ensure staff member belongs to the specified business
        if (staffMember.businessId != businessId) {
            throw StaffDomainException("Staff member does not belong to this business")
        }

        return mapToDTO(staffMember)
    }

    /**
     * Gets a staff member by email
     */
    @Transactional(readOnly = true)
    fun getStaffMemberByEmail(businessId: UUID, email: String): StaffMemberDTO? {
        return staffRepository.findByBusinessIdAndEmail(businessId, email.lowercase().trim())
            ?.let { mapToDTO(it) }
    }

    /**
     * Activates a staff member on their first login
     */
    @Transactional
    fun activateStaffMember(keycloakId: String): StaffMemberDTO {
        val staffMember = staffRepository.findByKeycloakId(keycloakId)
            ?: throw StaffDomainException("Staff member not found with Keycloak ID: $keycloakId")

        if (staffMember.isActive()) {
            return mapToDTO(staffMember)
        }

        staffMember.activate(keycloakId)
        val savedStaffMember = staffRepository.save(staffMember)

        return mapToDTO(savedStaffMember)
    }

    /**
     * Deactivates a staff member
     */
    @Transactional
    fun deactivateStaffMember(businessId: UUID, staffId: UUID): StaffMemberDTO {
        val staffMember = staffRepository.findById(staffId)
            .orElseThrow { StaffDomainException("Staff member not found with ID: $staffId") }

        // Ensure staff member belongs to the specified business
        if (staffMember.businessId != businessId) {
            throw StaffDomainException("Staff member does not belong to this business")
        }

        staffMember.deactivate()
        val savedStaffMember = staffRepository.save(staffMember)

        return mapToDTO(savedStaffMember)
    }

    /**
     * Updates a staff member's profile
     */
    @Transactional
    fun updateStaffMemberProfile(staffId: UUID, request: UpdateStaffProfileRequest): StaffMemberDTO {
        val staffMember = staffRepository.findById(staffId)
            .orElseThrow { StaffDomainException("Staff member not found with ID: $staffId") }

        staffMember.updateProfile(
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            jobTitle = request.jobTitle
        )

        val savedStaffMember = staffRepository.save(staffMember)

        return mapToDTO(savedStaffMember)
    }

    /**
     * Maps a staff member entity to DTO
     */
    private fun mapToDTO(staffMember: StaffMember): StaffMemberDTO {
        return StaffMemberDTO(
            id = staffMember.id,
            businessId = staffMember.businessId,
            firstName = staffMember.getFirstName(),
            lastName = staffMember.getLastName(),
            email = staffMember.getEmail(),
            phoneNumber = staffMember.getPhoneNumber(),
            jobTitle = staffMember.getJobTitle(),
            active = staffMember.isActive(),
            activatedAt = staffMember.activatedAt,
            createdAt = staffMember.createdAt,
            updatedAt = staffMember.updatedAt
        )
    }

    /**
     * Generates a temporary password for new staff members
     */
    private fun generateTemporaryPassword(): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..10).map { chars.random() }.joinToString("")
    }
}