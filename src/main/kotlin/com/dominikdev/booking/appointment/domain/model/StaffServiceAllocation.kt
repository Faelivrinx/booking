package com.dominikdev.booking.appointment.domain.model

import java.util.UUID

class StaffServiceAllocation(
    val id: UUID = UUID.randomUUID(),
    val staffId: UUID,
    val serviceId: UUID,
    val businessId: UUID
) {

}