package com.dominikdev.booking.business.staff

import com.dominikdev.booking.shared.infrastructure.security.BusinessOwnerSecurity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/staff/{staffId}/services")
class StaffServiceController(
    private val staffServiceAssociationService: StaffServiceAssociationService,
    private val staffService: StaffService
) {
    @GetMapping
    fun getStaffServices(
        @PathVariable businessId: String,
        @PathVariable staffId: String
    ): ResponseEntity<StaffServicesResponse> {
        val staffUuid = UUID.fromString(staffId)
        val businessUuid = UUID.fromString(businessId)

        // First verify that the staff member belongs to this business
        val staffMember = staffService.getStaffMemberById(businessUuid, staffUuid)

        // Get service IDs
        val serviceIds = staffServiceAssociationService.getServicesForStaff(staffUuid)

        return ResponseEntity.ok(StaffServicesResponse(serviceIds))
    }

    @PostMapping
    @BusinessOwnerSecurity
    fun setStaffServices(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @RequestBody request: StaffServicesRequest
    ): ResponseEntity<StaffServicesResponse> {
        val staffUuid = UUID.fromString(staffId)
        val businessUuid = UUID.fromString(businessId)

        // First verify that the staff member belongs to this business
        staffService.getStaffMemberById(businessUuid, staffUuid)

        // Set services
        staffServiceAssociationService.setStaffServices(staffUuid, request.serviceIds)

        // Get updated service IDs
        val updatedServiceIds = staffServiceAssociationService.getServicesForStaff(staffUuid)

        return ResponseEntity.ok(StaffServicesResponse(updatedServiceIds))
    }

    @PostMapping("/{serviceId}")
    @BusinessOwnerSecurity
    fun assignServiceToStaff(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Void> {
        val staffUuid = UUID.fromString(staffId)
        val businessUuid = UUID.fromString(businessId)
        val serviceUuid = UUID.fromString(serviceId)

        // First verify that the staff member belongs to this business
        staffService.getStaffMemberById(businessUuid, staffUuid)

        // Assign service
        staffServiceAssociationService.assignServiceToStaff(staffUuid, serviceUuid)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/{serviceId}")
    @BusinessOwnerSecurity
    fun removeServiceFromStaff(
        @PathVariable businessId: String,
        @PathVariable staffId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Void> {
        val staffUuid = UUID.fromString(staffId)
        val businessUuid = UUID.fromString(businessId)
        val serviceUuid = UUID.fromString(serviceId)

        // First verify that the staff member belongs to this business
        staffService.getStaffMemberById(businessUuid, staffUuid)

        // Remove service
        staffServiceAssociationService.removeServiceFromStaff(staffUuid, serviceUuid)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    data class StaffServicesRequest(
        val serviceIds: List<UUID>
    )

    data class StaffServicesResponse(
        val serviceIds: List<UUID>
    )
}