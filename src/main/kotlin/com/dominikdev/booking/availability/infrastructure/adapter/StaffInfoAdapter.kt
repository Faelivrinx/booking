package com.dominikdev.booking.availability.infrastructure.adapter

import java.util.UUID

interface StaffInfoAdapter {
    fun getStaffName(staffId: UUID): String?
    fun getBusinessName(businessId: UUID): String?
}