package com.dominikdev.booking.availability.infrastructure.readmodel

import java.util.UUID

interface StaffRepository {
    fun findStaffName(staffId: UUID): String?
}