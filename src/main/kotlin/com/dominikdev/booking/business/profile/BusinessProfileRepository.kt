package com.dominikdev.booking.business.profile

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BusinessProfileRepository : JpaRepository<BusinessProfileEntity, UUID> {
}