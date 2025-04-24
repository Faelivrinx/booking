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

    fun findByServiceIdAndDateOrderByStartTime(
        serviceId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    fun findByServiceIdAndDateBetweenOrderByDateAscStartTimeAsc(
        serviceId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailableBookingSlot>

    fun findByBusinessIdAndDateOrderByServiceNameAscStartTimeAsc(
        businessId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

    fun findByStaffIdAndDateOrderByStartTimeAsc(
        staffId: UUID,
        date: LocalDate
    ): List<AvailableBookingSlot>

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

    @Modifying
    @Query("DELETE FROM AvailableBookingSlot a WHERE a.serviceId = :serviceId AND a.date = :date")
    fun deleteByServiceIdAndDate(
        @Param("serviceId") serviceId: UUID,
        @Param("date") date: LocalDate
    )

    @Modifying
    @Query("DELETE FROM AvailableBookingSlot a WHERE a.staffId = :staffId AND a.date = :date")
    fun deleteByStaffIdAndDate(
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate
    )

    @Modifying
    @Query("""
        DELETE FROM AvailableBookingSlot a 
        WHERE a.staffId = :staffId 
        AND a.date = :date 
        AND ((a.startTime >= :startTime AND a.startTime < :endTime) 
        OR (a.endTime > :startTime AND a.endTime <= :endTime)
        OR (a.startTime <= :startTime AND a.endTime >= :endTime))
    """)
    fun deleteSlotInTimeRange(
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    )
}