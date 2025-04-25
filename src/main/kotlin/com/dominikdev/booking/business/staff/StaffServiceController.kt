package com.dominikdev.booking.business.staff

import com.dominikdev.booking.shared.infrastructure.security.BusinessOwnerSecurity
import com.dominikdev.booking.shared.infrastructure.security.ValidateServiceBelongsToBusiness
import com.dominikdev.booking.shared.infrastructure.security.ValidateStaffBelongsToBusiness
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/businesses/staff/{staffId}/services")
class StaffServiceController(
    private val staffServiceAssociationService: StaffServiceAssociationService,
    private val staffService: StaffService
) {
    @GetMapping
    fun getStaffServices(
        @PathVariable staffId: String
    ): ResponseEntity<StaffServicesResponse> {
        val staffUuid = UUID.fromString(staffId)

        // Get service IDs
        val serviceIds = staffServiceAssociationService.getServicesForStaff(staffUuid)

        return ResponseEntity.ok(StaffServicesResponse(serviceIds))
    }

    @PostMapping
    @ValidateStaffBelongsToBusiness
    fun setStaffServices(
        @PathVariable staffId: String,
        @RequestBody request: StaffServicesRequest
    ): ResponseEntity<StaffServicesResponse> {
        val staffUuid = UUID.fromString(staffId)

        // Set services
        staffServiceAssociationService.setStaffServices(staffUuid, request.serviceIds)

        // Get updated service IDs
        val updatedServiceIds = staffServiceAssociationService.getServicesForStaff(staffUuid)

        return ResponseEntity.ok(StaffServicesResponse(updatedServiceIds))
    }

    @PostMapping("/{serviceId}")
    @ValidateStaffBelongsToBusiness
    @ValidateServiceBelongsToBusiness
    fun assignServiceToStaff(
        @PathVariable staffId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Void> {
        val staffUuid = UUID.fromString(staffId)
        val serviceUuid = UUID.fromString(serviceId)

        // Assign service
        staffServiceAssociationService.assignServiceToStaff(staffUuid, serviceUuid)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/{serviceId}")
    fun removeServiceFromStaff(
        @PathVariable staffId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Void> {
        val staffUuid = UUID.fromString(staffId)
        val serviceUuid = UUID.fromString(serviceId)

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