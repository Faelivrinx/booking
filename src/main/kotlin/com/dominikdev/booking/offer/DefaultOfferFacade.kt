package com.dominikdev.booking.offer

import com.dominikdev.booking.offer.application.BusinessApplicationService
import com.dominikdev.booking.offer.application.ServiceApplicationService
import com.dominikdev.booking.offer.application.StaffApplicationService
import com.dominikdev.booking.offer.application.StaffServiceAssignmentApplicationService
import com.dominikdev.booking.offer.domain.Business
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

class DefaultOfferFacade(
    private val businessApplicationService: BusinessApplicationService,
    private val serviceApplicationService: ServiceApplicationService,
    private val staffApplicationService: StaffApplicationService,
    private val staffServiceAssignmentApplicationService: StaffServiceAssignmentApplicationService
) : OfferFacade {

    private val logger = LoggerFactory.getLogger(DefaultOfferFacade::class.java)

    // Business Management
    override fun createBusiness(request: CreateBusinessRequest): BusinessProfile {
        logger.info("Creating business: ${request.name}")

        val business = businessApplicationService.createBusiness(request)
        return mapToBusinessProfile(business).also {
            logger.info("Successfully created business: ${it.id}")
        }
    }

    override fun updateBusiness(businessId: UUID, request: UpdateBusinessRequest): BusinessProfile {
        logger.info("Updating business: $businessId")

        val business = businessApplicationService.updateBusiness(businessId, request)
        return mapToBusinessProfile(business).also {
            logger.info("Successfully updated business: $businessId")
        }
    }

    override fun getBusiness(businessId: UUID): BusinessProfile? {
        logger.debug("Retrieving business: {}", businessId)

        return businessApplicationService.getBusiness(businessId)
            ?.let { mapToBusinessProfile(it) }
    }

    // Service Management
    override fun addService(businessId: UUID, request: AddServiceRequest): Service {
        logger.info("Adding service '${request.name}' to business: $businessId")

        val service = serviceApplicationService.addService(businessId, request)
        return mapToService(service).also {
            logger.info("Successfully added service: ${it.id}")
        }
    }

    override fun updateService(businessId: UUID, serviceId: UUID, request: UpdateServiceRequest): Service {
        logger.info("Updating service: $serviceId")

        val service = serviceApplicationService.updateService(businessId, serviceId, request)
        return mapToService(service).also {
            logger.info("Successfully updated service: $serviceId")
        }
    }

    override fun removeService(businessId: UUID, serviceId: UUID) {
        logger.info("Removing service: $serviceId")

        serviceApplicationService.removeService(businessId, serviceId)
        logger.info("Successfully removed service: $serviceId")
    }

    override fun getBusinessServices(businessId: UUID): List<Service> {
        logger.debug("Retrieving services for business: {}", businessId)

        return serviceApplicationService.getBusinessServices(businessId)
            .map { mapToService(it) }
    }

    override fun getService(businessId: UUID, serviceId: UUID): Service? {
        logger.debug("Retrieving service: {}", serviceId)

        return serviceApplicationService.getService(businessId, serviceId)
            ?.let { mapToService(it) }
    }

    // Staff Management
    override fun addStaffMember(businessId: UUID, request: AddStaffMemberRequest): StaffMember {
        logger.info("Adding staff member '${request.email}' to business: $businessId")

        val staffMember = staffApplicationService.addStaffMember(businessId, request)
        return mapToStaffMember(staffMember).also {
            logger.info("Successfully added staff member: ${it.id}")
        }
    }

    override fun updateStaffMember(businessId: UUID, staffId: UUID, request: UpdateStaffMemberRequest): StaffMember {
        logger.info("Updating staff member: $staffId")

        val staffMember = staffApplicationService.updateStaffMember(businessId, staffId, request)
        return mapToStaffMember(staffMember).also {
            logger.info("Successfully updated staff member: $staffId")
        }
    }

    override fun deactivateStaffMember(businessId: UUID, staffId: UUID) {
        logger.info("Deactivating staff member: $staffId")

        staffApplicationService.deactivateStaffMember(businessId, staffId)
        logger.info("Successfully deactivated staff member: $staffId")
    }

    override fun getBusinessStaff(businessId: UUID): List<StaffMember> {
        logger.debug("Retrieving staff for business: {}", businessId)

        return staffApplicationService.getBusinessStaff(businessId)
            .map { mapToStaffMember(it) }
    }

    override fun getStaffMember(businessId: UUID, staffId: UUID): StaffMember? {
        logger.debug("Retrieving staff member: {}", staffId)

        return staffApplicationService.getStaffMember(businessId, staffId)
            ?.let { mapToStaffMember(it) }
    }

    // Staff Service Assignments
    override fun assignServiceToStaff(businessId: UUID, staffId: UUID, serviceId: UUID) {
        logger.info("Assigning service $serviceId to staff $staffId")

        staffServiceAssignmentApplicationService.assignServiceToStaff(businessId, staffId, serviceId)
        logger.info("Successfully assigned service to staff")
    }

    override fun unassignServiceFromStaff(businessId: UUID, staffId: UUID, serviceId: UUID) {
        logger.info("Unassigning service $serviceId from staff $staffId")

        staffServiceAssignmentApplicationService.unassignServiceFromStaff(businessId, staffId, serviceId)
        logger.info("Successfully unassigned service from staff")
    }

    override fun setStaffServices(businessId: UUID, staffId: UUID, serviceIds: List<UUID>) {
        logger.info("Setting ${serviceIds.size} services for staff $staffId")

        staffServiceAssignmentApplicationService.setStaffServices(businessId, staffId, serviceIds)
        logger.info("Successfully set staff services")
    }

    override fun getStaffServices(businessId: UUID, staffId: UUID): List<Service> {
        logger.debug("Retrieving services for staff {}", staffId)

        return staffServiceAssignmentApplicationService.getStaffServices(businessId, staffId)
            .map { mapToService(it) }
    }

    override fun getServiceStaff(businessId: UUID, serviceId: UUID): List<StaffMember> {
        logger.debug("Retrieving staff for service {}", serviceId)

        return staffServiceAssignmentApplicationService.getServiceStaff(businessId, serviceId)
            .map { mapToStaffMember(it) }
    }

    // Queries for other contexts
    override fun getServiceDuration(serviceId: UUID): Int? {
        logger.debug("Getting duration for service: {}", serviceId)
        return serviceApplicationService.getServiceDuration(serviceId)
    }

    override fun getServicePrice(serviceId: UUID): BigDecimal? {
        logger.debug("Getting price for service: {}", serviceId)
        return serviceApplicationService.getServicePrice(serviceId)
    }

    override fun isStaffMemberActive(staffId: UUID): Boolean {
        logger.debug("Checking if staff member is active: {}", staffId)
        return staffApplicationService.isStaffMemberActive(staffId)
    }

    override fun getStaffMemberBusinessId(staffId: UUID): UUID? {
        logger.debug("Getting business ID for staff member: {}", staffId)
        return staffApplicationService.getStaffMemberBusinessId(staffId)
    }

    // Private mapping methods
    private fun mapToBusinessProfile(business: Business): BusinessProfile {
        return BusinessProfile(
            id = business.id,
            name = business.name,
            description = business.description,
            address = BusinessAddress(
                street = business.address.street,
                city = business.address.city,
                state = business.address.state,
                postalCode = business.address.postalCode
            ),
            ownerId = business.ownerId,
            isActive = business.isActive,
            createdAt = business.createdAt,
            updatedAt = business.updatedAt
        )
    }

    private fun mapToService(service: com.dominikdev.booking.offer.domain.Service): Service {
        return Service(
            id = service.id,
            businessId = service.businessId,
            name = service.name,
            description = service.description,
            durationMinutes = service.durationMinutes,
            price = service.price,
            isActive = service.isActive,
            createdAt = service.createdAt,
            updatedAt = service.updatedAt
        )
    }

    private fun mapToStaffMember(staffMember: com.dominikdev.booking.offer.domain.StaffMember): StaffMember {
        return StaffMember(
            id = staffMember.id,
            businessId = staffMember.businessId,
            keycloakId = staffMember.keycloakId,
            firstName = staffMember.firstName,
            lastName = staffMember.lastName,
            email = staffMember.email,
            phoneNumber = staffMember.phoneNumber,
            jobTitle = staffMember.jobTitle,
            isActive = staffMember.isActive,
            createdAt = staffMember.createdAt,
            updatedAt = staffMember.updatedAt
        )
    }
}