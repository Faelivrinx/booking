package com.dominikdev.booking.offer.infrastructure.web

import com.dominikdev.booking.offer.*
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin(origins = ["http://localhost:3000", "http://localhost:8080"])
class BusinessController(
    private val offerFacade: OfferFacade
) {

    private val logger = LoggerFactory.getLogger(BusinessController::class.java)

    // Business Profile Management

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createBusiness(@Valid @RequestBody request: CreateBusinessRequest): ResponseEntity<*> {
        logger.info("Creating business: ${request.name}")

        return try {
            val businessProfile = offerFacade.createBusiness(request)
            logger.info("Successfully created business: ${businessProfile.id}")
            ResponseEntity.status(HttpStatus.CREATED).body(businessProfile)
        } catch (e: Exception) {
            logger.error("Failed to create business: ${request.name}", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "BUSINESS_CREATION_FAILED",
                    message = e.message ?: "Failed to create business",
                    details = "Please verify the business information and try again"
                )
            )
        }
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun getBusiness(@PathVariable businessId: UUID): ResponseEntity<*> {
        logger.debug("Retrieving business: $businessId")

        return try {
            val businessProfile = offerFacade.getBusiness(businessId)
            if (businessProfile != null) {
                ResponseEntity.ok(businessProfile)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "BUSINESS_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve business",
                    details = "Business may not exist or be accessible"
                )
            )
        }
    }

    @PutMapping("/{businessId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER')")
    fun updateBusiness(
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: UpdateBusinessRequest
    ): ResponseEntity<*> {
        logger.info("Updating business: $businessId")

        return try {
            val businessProfile = offerFacade.updateBusiness(businessId, request)
            logger.info("Successfully updated business: $businessId")
            ResponseEntity.ok(businessProfile)
        } catch (e: Exception) {
            logger.error("Failed to update business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "BUSINESS_UPDATE_FAILED",
                    message = e.message ?: "Failed to update business",
                    details = "Please verify the business information and permissions"
                )
            )
        }
    }

    // Service Management

    @GetMapping("/{businessId}/services")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun getBusinessServices(@PathVariable businessId: UUID): ResponseEntity<*> {
        logger.debug("Retrieving services for business: $businessId")

        return try {
            val services = offerFacade.getBusinessServices(businessId)
            ResponseEntity.ok(
                ServicesResponse(
                    businessId = businessId,
                    services = services,
                    totalCount = services.size
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to retrieve services for business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICES_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve services",
                    details = "Please verify permissions and business access"
                )
            )
        }
    }

    @GetMapping("/{businessId}/services/{serviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun getService(
        @PathVariable businessId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<*> {
        logger.debug("Retrieving service: $serviceId")

        return try {
            val service = offerFacade.getService(businessId, serviceId)
            if (service != null) {
                ResponseEntity.ok(service)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve service: $serviceId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICE_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve service",
                    details = "Service may not exist or be accessible"
                )
            )
        }
    }

    @PostMapping("/{businessId}/services")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun addService(
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: AddServiceRequest
    ): ResponseEntity<*> {
        logger.info("Adding service '${request.name}' to business: $businessId")

        return try {
            val service = offerFacade.addService(businessId, request)
            logger.info("Successfully added service: ${service.id}")
            ResponseEntity.status(HttpStatus.CREATED).body(service)
        } catch (e: Exception) {
            logger.error("Failed to add service to business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICE_CREATION_FAILED",
                    message = e.message ?: "Failed to add service",
                    details = "Please verify the service information and permissions"
                )
            )
        }
    }

    @PutMapping("/{businessId}/services/{serviceId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun updateService(
        @PathVariable businessId: UUID,
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: UpdateServiceRequest
    ): ResponseEntity<*> {
        logger.info("Updating service: $serviceId")

        return try {
            val service = offerFacade.updateService(businessId, serviceId, request)
            logger.info("Successfully updated service: $serviceId")
            ResponseEntity.ok(service)
        } catch (e: Exception) {
            logger.error("Failed to update service: $serviceId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICE_UPDATE_FAILED",
                    message = e.message ?: "Failed to update service",
                    details = "Please verify the service information and permissions"
                )
            )
        }
    }

    @DeleteMapping("/{businessId}/services/{serviceId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun removeService(
        @PathVariable businessId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<*> {
        logger.info("Removing service: $serviceId")

        return try {
            offerFacade.removeService(businessId, serviceId)
            logger.info("Successfully removed service: $serviceId")
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Service removed successfully",
                    details = "Service and all related assignments have been removed"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to remove service: $serviceId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICE_REMOVAL_FAILED",
                    message = e.message ?: "Failed to remove service",
                    details = "Please verify permissions and try again"
                )
            )
        }
    }

    // Staff Management

    @GetMapping("/{businessId}/staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun getBusinessStaff(@PathVariable businessId: UUID): ResponseEntity<*> {
        logger.debug("Retrieving staff for business: $businessId")

        return try {
            val staff = offerFacade.getBusinessStaff(businessId)
            ResponseEntity.ok(
                StaffResponse(
                    businessId = businessId,
                    staff = staff,
                    totalCount = staff.size
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to retrieve staff for business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "STAFF_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve staff",
                    details = "Please verify permissions and business access"
                )
            )
        }
    }

    @GetMapping("/{businessId}/staff/{staffId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun getStaffMember(
        @PathVariable businessId: UUID,
        @PathVariable staffId: UUID
    ): ResponseEntity<*> {
        logger.debug("Retrieving staff member: $staffId")

        return try {
            val staffMember = offerFacade.getStaffMember(businessId, staffId)
            if (staffMember != null) {
                ResponseEntity.ok(staffMember)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve staff member: $staffId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "STAFF_MEMBER_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve staff member",
                    details = "Staff member may not exist or be accessible"
                )
            )
        }
    }

    @PostMapping("/{businessId}/staff")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun addStaffMember(
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: AddStaffMemberRequest
    ): ResponseEntity<*> {
        logger.info("Adding staff member '${request.email}' to business: $businessId")

        return try {
            val staffMember = offerFacade.addStaffMember(businessId, request)
            logger.info("Successfully added staff member: ${staffMember.id}")
            ResponseEntity.status(HttpStatus.CREATED).body(staffMember)
        } catch (e: Exception) {
            logger.error("Failed to add staff member to business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "STAFF_MEMBER_CREATION_FAILED",
                    message = e.message ?: "Failed to add staff member",
                    details = "Please verify the staff information and permissions"
                )
            )
        }
    }

    @PostMapping("/{businessId}/staff/{staffId}/deactivate")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun deactivateStaffMember(
        @PathVariable businessId: UUID,
        @PathVariable staffId: UUID
    ): ResponseEntity<*> {
        logger.info("Deactivating staff member: $staffId")

        return try {
            offerFacade.deactivateStaffMember(businessId, staffId)
            logger.info("Successfully deactivated staff member: $staffId")
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Staff member deactivated successfully",
                    details = "The staff member will no longer be able to access the system"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to deactivate staff member: $staffId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "STAFF_MEMBER_DEACTIVATION_FAILED",
                    message = e.message ?: "Failed to deactivate staff member",
                    details = "Please verify permissions and try again"
                )
            )
        }
    }

    @PutMapping("/{businessId}/staff/profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    fun updateStaffProfile(
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: UpdateStaffMemberRequest
    ): ResponseEntity<*> {
        logger.info("Staff member updating their profile")

        return try {
            // TODO: Implement staff self-profile update
            // Need to extract current staff ID from JWT token
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Profile update functionality coming soon",
                    details = "Staff members will be able to update their own profiles"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to update staff profile", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "PROFILE_UPDATE_FAILED",
                    message = e.message ?: "Failed to update profile",
                    details = "Please try again"
                )
            )
        }
    }

    // Staff Service Assignments

    @GetMapping("/{businessId}/staff/{staffId}/services")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun getStaffServices(
        @PathVariable businessId: UUID,
        @PathVariable staffId: UUID
    ): ResponseEntity<*> {
        logger.debug("Retrieving services for staff: $staffId")

        return try {
            val services = offerFacade.getStaffServices(businessId, staffId)
            ResponseEntity.ok(
                StaffServicesResponse(
                    businessId = businessId,
                    staffId = staffId,
                    services = services,
                    totalCount = services.size
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to retrieve services for staff: $staffId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "STAFF_SERVICES_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve staff services",
                    details = "Please verify permissions and access"
                )
            )
        }
    }

    @PostMapping("/{businessId}/staff/{staffId}/services")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun setStaffServices(
        @PathVariable businessId: UUID,
        @PathVariable staffId: UUID,
        @Valid @RequestBody request: SetStaffServicesRequest
    ): ResponseEntity<*> {
        logger.info("Setting ${request.serviceIds.size} services for staff: $staffId")

        return try {
            offerFacade.setStaffServices(businessId, staffId, request.serviceIds)
            logger.info("Successfully set services for staff: $staffId")
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Staff services updated successfully",
                    details = "Staff member can now perform ${request.serviceIds.size} services"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to set services for staff: $staffId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "STAFF_SERVICES_UPDATE_FAILED",
                    message = e.message ?: "Failed to update staff services",
                    details = "Please verify the service IDs and permissions"
                )
            )
        }
    }

    @PostMapping("/{businessId}/staff/{staffId}/services/{serviceId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun assignServiceToStaff(
        @PathVariable businessId: UUID,
        @PathVariable staffId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<*> {
        logger.info("Assigning service $serviceId to staff: $staffId")

        return try {
            offerFacade.assignServiceToStaff(businessId, staffId, serviceId)
            logger.info("Successfully assigned service to staff")
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Service assigned to staff member successfully",
                    details = "Staff member can now perform this service"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to assign service to staff", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICE_ASSIGNMENT_FAILED",
                    message = e.message ?: "Failed to assign service",
                    details = "Please verify the service ID and permissions"
                )
            )
        }
    }

    @DeleteMapping("/{businessId}/staff/{staffId}/services/{serviceId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('EMPLOYEE')")
    fun unassignServiceFromStaff(
        @PathVariable businessId: UUID,
        @PathVariable staffId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<*> {
        logger.info("Unassigning service $serviceId from staff: $staffId")

        return try {
            offerFacade.unassignServiceFromStaff(businessId, staffId, serviceId)
            logger.info("Successfully unassigned service from staff")
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Service unassigned from staff member successfully",
                    details = "Staff member can no longer perform this service"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to unassign service from staff", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "SERVICE_UNASSIGNMENT_FAILED",
                    message = e.message ?: "Failed to unassign service",
                    details = "Please verify the service ID and permissions"
                )
            )
        }
    }

    // Health Check
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<*> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "service" to "Business Service",
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
}

// Request DTOs
data class SetStaffServicesRequest(
    val serviceIds: List<UUID>
)

// Response DTOs
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SuccessResponse(
    val message: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ServicesResponse(
    val businessId: UUID,
    val services: List<Service>,
    val totalCount: Int
)

data class StaffResponse(
    val businessId: UUID,
    val staff: List<StaffMember>,
    val totalCount: Int
)

data class StaffServicesResponse(
    val businessId: UUID,
    val staffId: UUID,
    val services: List<Service>,
    val totalCount: Int
)