package com.dominikdev.booking.appointment.infrastructure.readmodel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

/**
 * Repository for client appointment views
 */
@Repository
interface ClientAppointmentViewRepository : JpaRepository<ClientAppointmentView, UUID> {

    /**
     * Find appointments for a client
     */
    fun findByClientIdOrderByDateDescStartTimeDesc(
        clientId: UUID
    ): List<ClientAppointmentView>

    /**
     * Find appointments for a client in a date range
     */
    fun findByClientIdAndDateBetweenOrderByDateAscStartTimeAsc(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ClientAppointmentView>

    /**
     * Find appointments for a client with a specific status
     */
    fun findByClientIdAndStatusOrderByDateDescStartTimeDesc(
        clientId: UUID,
        status: String
    ): List<ClientAppointmentView>

    /**
     * Find upcoming appointments for a client
     */
    @Query("""
        SELECT a FROM ClientAppointmentView a 
        WHERE a.clientId = :clientId 
        AND a.date >= :today 
        AND a.status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')
        ORDER BY a.date ASC, a.startTime ASC
    """)
    fun findUpcomingAppointments(
        @Param("clientId") clientId: UUID,
        @Param("today") today: LocalDate
    ): List<ClientAppointmentView>
}