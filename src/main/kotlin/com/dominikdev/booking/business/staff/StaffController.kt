package com.dominikdev.booking.business.staff

import com.dominikdev.booking.shared.infrastructure.security.BusinessOwnerSecurity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/staff")
class StaffController(private val staffService: StaffService) {

    @GetMapping
    @BusinessOwnerSecurity
    fun getAllStaffMembers(@PathVariable businessId: String): ResponseEntity<List<StaffMemberDTO>> {
        val staffMembers = staffService.getStaffMembers(UUID.fromString(businessId))
        return ResponseEntity.ok(staffMembers)
    }

    @GetMapping("/{staffId}")
    @BusinessOwnerSecurity
    fun getStaffMember(
        @PathVariable businessId: String,
        @PathVariable staffId: String
    ): ResponseEntity<StaffMemberDTO> {
        val staffMember = staffService.getStaffMemberById(
            UUID.fromString(businessId),
            UUID.fromString(staffId)
        )
        return ResponseEntity.ok(staffMember)
    }

    @PostMapping
    @BusinessOwnerSecurity
    fun createStaffMember(
        @PathVariable businessId: String,
        @RequestBody request: CreateStaffMemberRequest
    ): ResponseEntity<StaffMemberDTO> {
        val createRequest = CreateStaffRequest(
            businessId = UUID.fromString(businessId),
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phoneNumber = request.phoneNumber,
            jobTitle = request.jobTitle,
            businessName = request.businessName
        )

        val staffMember = staffService.createStaffMember(createRequest)
        return ResponseEntity(staffMember, HttpStatus.CREATED)
    }

    @PostMapping("/{staffId}/deactivate")
    @BusinessOwnerSecurity
    fun deactivateStaffMember(
        @PathVariable businessId: String,
        @PathVariable staffId: String
    ): ResponseEntity<StaffMemberDTO> {
        val staffMember = staffService.deactivateStaffMember(
            UUID.fromString(businessId),
            UUID.fromString(staffId)
        )
        return ResponseEntity.ok(staffMember)
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('STAFF_MEMBER')")
    fun updateOwnProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<StaffMemberDTO> {
        // Extract the keycloak ID from the JWT
        val keycloakId = jwt.subject

        // Find the staff member by their keycloak ID
        val staffMember = staffService.activateStaffMember(keycloakId)

        // Update their profile
        val updateRequest = UpdateStaffProfileRequest(
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            jobTitle = request.jobTitle
        )

        val updatedStaffMember = staffService.updateStaffMemberProfile(staffMember.id, updateRequest)
        return ResponseEntity.ok(updatedStaffMember)
    }

    data class CreateStaffMemberRequest(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phoneNumber: String?,
        val jobTitle: String?,
        val businessName: String
    )

    data class UpdateProfileRequest(
        val firstName: String,
        val lastName: String,
        val phoneNumber: String?,
        val jobTitle: String?
    )
}