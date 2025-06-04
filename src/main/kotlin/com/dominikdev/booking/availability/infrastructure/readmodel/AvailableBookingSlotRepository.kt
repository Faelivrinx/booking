package com.dominikdev.booking.availability.infrastructure.readmodel


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Repository
interface AvailableBookingSlotRepository : JpaRepository<AvailableBookingSlot, UUID> {

    // Find by business and service for a specific date
    fun findByBusinessIdAndServiceIdAndDateOrderByStartTime(
        businessId: UUID,
        serviceId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    // Find by business and service for a date range
    fun findByBusinessIdAndServiceIdAndDateBetweenOrderByDateAscStartTimeAsc(
        businessId: UUID,
        serviceId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailableBookingSlot>

    // Find by business and staff and service for a specific date
    fun findByBusinessIdAndStaffIdAndServiceIdAndDate(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    // Find days that have available slots for a business and service within a date range
    @Query("""
        SELECT DISTINCT a.date FROM AvailableBookingSlot a
        WHERE a.businessId = :businessId
        AND a.serviceId = :serviceId
        AND a.date BETWEEN :startDate AND :endDate
        ORDER BY a.date ASC
    """)
    fun findDaysWithAvailableSlotsByBusinessIdAndServiceId(
        @Param("businessId") businessId: UUID,
        @Param("serviceId") serviceId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<LocalDate>

    // Find by business and service for upcoming dates (pageable)
    @Query("""
        SELECT a FROM AvailableBookingSlot a 
        WHERE a.businessId = :businessId
        AND a.serviceId = :serviceId 
        AND a.date >= :fromDate 
        ORDER BY a.date ASC, a.startTime ASC
    """)
    fun findNextAvailableSlots(
        @Param("businessId") businessId: UUID,
        @Param("serviceId") serviceId: UUID,
        @Param("fromDate") fromDate: LocalDate,
        pageable: org.springframework.data.domain.Pageable
    ): List<AvailableBookingSlot>

    @Modifying
    @Query("DELETE FROM AvailableBookingSlot a WHERE a.businessId = :businessId AND a.serviceId = :serviceId AND a.date = :date")
    fun deleteByBusinessIdAndServiceIdAndDate(
        @Param("businessId") businessId: UUID,
        @Param("serviceId") serviceId: UUID,
        @Param("date") date: LocalDate
    )

    @Modifying
    @Query("DELETE FROM AvailableBookingSlot a WHERE a.businessId = :businessId AND a.staffId = :staffId AND a.date = :date")
    fun deleteByBusinessIdAndStaffIdAndDate(
        @Param("businessId") businessId: UUID,
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate
    )

    @Modifying
    @Query("""
    DELETE FROM AvailableBookingSlot a 
    WHERE a.businessId = :businessId
    AND a.staffId = :staffId 
    AND a.date = :date 
    AND ((a.startTime >= :startTime AND a.startTime < :endTime) 
    OR (a.endTime > :startTime AND a.endTime <= :endTime)
    OR (a.startTime <= :startTime AND a.endTime >= :endTime))
""")
    fun deleteSlotInTimeRange(
        @Param("businessId") businessId: UUID,
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    )

    /**
     * Find a specific available slot by all key fields
     * Used to verify slot availability before booking
     */
    fun findByBusinessIdAndStaffIdAndServiceIdAndDateAndStartTime(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate,
        startTime: LocalTime
    ): AvailableBookingSlot?

    /**
     * Find available slots for a service with optional staff filter
     * Used for finding alternatives when preferred slot is taken
     */
    @Query("""
        SELECT a FROM AvailableBookingSlot a 
        WHERE a.businessId = :businessId
        AND a.serviceId = :serviceId
        AND a.date = :date
        AND (:staffId IS NULL OR a.staffId = :staffId)
        ORDER BY a.startTime ASC
    """)
    fun findAvailableSlotsWithOptionalStaff(
        @Param("businessId") businessId: UUID,
        @Param("serviceId") serviceId: UUID,
        @Param("date") date: LocalDate,
        @Param("staffId") staffId: UUID?
    ): List<AvailableBookingSlot>

    /**
     * Check if a specific slot exists (for quick availability check)
     */
    fun existsByBusinessIdAndStaffIdAndServiceIdAndDateAndStartTime(
        businessId: UUID,
        staffId: UUID,
        serviceId: UUID,
        date: LocalDate,
        startTime: LocalTime
    ): Boolean
}