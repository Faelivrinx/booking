package com.dominikdev.booking.appointment.infrastructure.readmodel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Repository for querying available booking slots
 */
@Repository
interface AvailableBookingSlotRepository : JpaRepository<AvailableBookingSlot, UUID> {

    /**
     * Find available slots for a specific service and date
     */
    fun findByServiceIdAndDateOrderByStartTime(
        serviceId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    /**
     * Find available slots for a specific service within a date range
     */
    fun findByServiceIdAndDateBetweenOrderByDateAscStartTimeAsc(
        serviceId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailableBookingSlot>

    /**
     * Find available slots for a business on a specific date
     */
    fun findByBusinessIdAndDateOrderByServiceNameAscStartTimeAsc(
        businessId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    /**
     * Find available slots for a specific staff member on a date
     */
    fun findByStaffIdAndDateOrderByStartTimeAsc(
        staffId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    /**
     * Find next available slots for a service starting from a date
     */
    @Query("""
        SELECT a FROM AvailableBookingSlot a 
        WHERE a.serviceId = :serviceId 
        AND a.date >= :fromDate 
        ORDER BY a.date ASC, a.startTime ASC
    """)
    fun findNextAvailableSlots(
        @Param("serviceId") serviceId: UUID,
        @Param("fromDate") fromDate: LocalDate,
        pageable: org.springframework.data.domain.Pageable
    ): List<AvailableBookingSlot>

    /**
     * Delete slots for a service on a specific date
     */
    fun deleteByServiceIdAndDate(serviceId: UUID, date: LocalDate)

    /**
     * Delete slots for a staff member on a specific date
     */
    fun deleteByStaffIdAndDate(staffId: UUID, date: LocalDate)

    /**
     * Delete slots in a specific time range for a staff
     */
    @Query("""
        DELETE FROM AvailableBookingSlot a 
        WHERE a.staffId = :staffId 
        AND a.date = :date 
        AND a.startTime >= :startTime 
        AND a.endTime <= :endTime
    """)
    fun deleteSlotInTimeRange(
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    )
}