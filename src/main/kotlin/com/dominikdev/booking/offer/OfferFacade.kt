package com.dominikdev.booking.offer

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

interface OfferFacade {
    // Business Management
    fun createBusiness(request: CreateBusinessRequest): BusinessProfile
    fun updateBusiness(businessId: UUID, request: UpdateBusinessRequest): BusinessProfile
    fun getBusiness(businessId: UUID): BusinessProfile?

    // Service Management
    fun addService(businessId: UUID, request: AddServiceRequest): Service
    fun updateService(businessId: UUID, serviceId: UUID, request: UpdateServiceRequest): Service
    fun removeService(businessId: UUID, serviceId: UUID)
    fun getBusinessServices(businessId: UUID): List<Service>
    fun getService(businessId: UUID, serviceId: UUID): Service?

    // Staff Management
    fun addStaffMember(businessId: UUID, request: AddStaffMemberRequest): StaffMember
    fun updateStaffMember(businessId: UUID, staffId: UUID, request: UpdateStaffMemberRequest): StaffMember
    fun deactivateStaffMember(businessId: UUID, staffId: UUID)
    fun getBusinessStaff(businessId: UUID): List<StaffMember>
    fun getStaffMember(businessId: UUID, staffId: UUID): StaffMember?

    // Staff Service Assignments
    fun assignServiceToStaff(businessId: UUID, staffId: UUID, serviceId: UUID)
    fun unassignServiceFromStaff(businessId: UUID, staffId: UUID, serviceId: UUID)
    fun setStaffServices(businessId: UUID, staffId: UUID, serviceIds: List<UUID>)
    fun getStaffServices(businessId: UUID, staffId: UUID): List<Service>
    fun getServiceStaff(businessId: UUID, serviceId: UUID): List<StaffMember>

    // Queries for other contexts
    fun getServiceDuration(serviceId: UUID): Int?
    fun getServicePrice(serviceId: UUID): BigDecimal?
    fun isStaffMemberActive(staffId: UUID): Boolean
    fun getStaffMemberBusinessId(staffId: UUID): UUID?
}

data class CreateBusinessRequest(
    val name: String,
    val description: String?,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String?,
    val ownerPassword: String
)

data class UpdateBusinessRequest(
    val name: String,
    val description: String?,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String
)

data class AddServiceRequest(
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val price: BigDecimal
)

data class UpdateServiceRequest(
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val price: BigDecimal
)

data class AddStaffMemberRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val jobTitle: String?,
    val businessName: String
)

data class UpdateStaffMemberRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val jobTitle: String?
)

data class BusinessProfile(
    val id: UUID,
    val name: String,
    val description: String?,
    val address: BusinessAddress,
    val ownerId: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BusinessAddress(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String
) {
    fun getFullAddress(): String = "$street, $city, $state $postalCode"
}

data class Service(
    val id: UUID,
    val businessId: UUID,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val price: BigDecimal,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class StaffMember(
    val id: UUID,
    val businessId: UUID,
    val keycloakId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val jobTitle: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun getFullName(): String = "$firstName $lastName"
}