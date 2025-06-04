package com.dominikdev.booking.appointment.infrastructure.repository

import com.dominikdev.booking.appointment.domain.model.Appointment
import com.dominikdev.booking.appointment.domain.model.AppointmentStatus
import com.dominikdev.booking.appointment.domain.repository.AppointmentRepository
import com.dominikdev.booking.appointment.infrastructure.entity.AppointmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

/**
 * JPA Repository for querying AppointmentEntity
 */
interface AppointmentJpaRepository : JpaRepository<AppointmentEntity, UUID> {
    fun findByClientId(clientId: UUID): List<AppointmentEntity>

    fun findByClientIdAndStatusIn(clientId: UUID, statuses: List<AppointmentStatus>): List<AppointmentEntity>

    fun findByClientIdAndDateBetween(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AppointmentEntity>

    fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): List<AppointmentEntity>

    @Query("""
        SELECT a FROM AppointmentEntity a 
        WHERE a.businessId = :businessId 
        AND a.date >= :fromDate
        AND a.status IN ('SCHEDULED', 'CONFIRMED')
        ORDER BY a.date ASC, a.startTime ASC
    """)
    fun findPendingAppointmentsByBusiness(
        @Param("businessId") businessId: UUID,
        @Param("fromDate") fromDate: LocalDate
    ): List<AppointmentEntity>

    fun findByStaffId(staffId: UUID): List<AppointmentEntity>

    fun findByStaffIdAndDateBetween(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AppointmentEntity>

    fun findByStaffIdAndStatusInAndDateAfter(
        staffId: UUID,
        statuses: List<AppointmentStatus>,
        date: LocalDate
    ): List<AppointmentEntity>
}

/**
 * Implementation of AppointmentRepository using JPA
 */
@Repository
class JpaAppointmentRepository(
    private val appointmentJpaRepository: AppointmentJpaRepository
) : AppointmentRepository {

    override fun save(appointment: Appointment): Appointment {
        val entity = mapToEntity(appointment)
        val savedEntity = appointmentJpaRepository.save(entity)
        return mapToDomain(savedEntity)
    }

    override fun findById(id: UUID): Appointment? {
        return appointmentJpaRepository.findById(id)
            .map { mapToDomain(it) }
            .orElse(null)
    }

    override fun findByClientId(clientId: UUID): List<Appointment> {
        return appointmentJpaRepository.findByClientId(clientId)
            .map { mapToDomain(it) }
    }

    override fun findByClientIdAndStatuses(
        clientId: UUID,
        statuses: List<AppointmentStatus>
    ): List<Appointment> {
        return appointmentJpaRepository.findByClientIdAndStatusIn(clientId, statuses)
            .map { mapToDomain(it) }
    }

    override fun findByClientIdAndDateRange(
        clientId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment> {
        return appointmentJpaRepository.findByClientIdAndDateBetween(clientId, startDate, endDate)
            .map { mapToDomain(it) }
    }

    override fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): List<Appointment> {
        return appointmentJpaRepository.findByStaffIdAndDate(staffId, date)
            .map { mapToDomain(it) }
    }

    override fun findPendingAppointmentsByBusiness(
        businessId: UUID,
        fromDate: LocalDate
    ): List<Appointment> {
        return appointmentJpaRepository.findPendingAppointmentsByBusiness(businessId, fromDate)
            .map { mapToDomain(it) }
    }

    override fun delete(id: UUID) {
        appointmentJpaRepository.deleteById(id)
    }

    override fun findByStaffId(staffId: UUID): List<Appointment> {
        return appointmentJpaRepository.findByStaffId(staffId)
            .map { mapToDomain(it) }
    }

    override fun findByStaffIdAndDateRange(
        staffId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment> {
        return appointmentJpaRepository.findByStaffIdAndDateBetween(staffId, startDate, endDate)
            .map { mapToDomain(it) }
    }

    override fun findByStaffIdAndStatusesAndDateAfter(
        staffId: UUID,
        statuses: List<AppointmentStatus>,
        date: LocalDate
    ): List<Appointment> {
        return appointmentJpaRepository.findByStaffIdAndStatusInAndDateAfter(staffId, statuses, date)
            .map { mapToDomain(it) }
    }

    /**
     * Maps a domain Appointment to an AppointmentEntity
     */
    private fun mapToEntity(appointment: Appointment): AppointmentEntity {
        return AppointmentEntity(
            id = appointment.id,
            businessId = appointment.businessId,
            clientId = appointment.clientId,
            staffId = appointment.staffId,
            serviceId = appointment.serviceId,
            date = appointment.date,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            status = appointment.status,
            notes = appointment.notes,
            createdAt = appointment.createdAt,
            updatedAt = appointment.updatedAt
        )
    }

    /**
     * Maps an AppointmentEntity to a domain Appointment
     */
    private fun mapToDomain(entity: AppointmentEntity): Appointment {
        return Appointment.reconstitute(
            id = entity.id,
            businessId = entity.businessId,
            clientId = entity.clientId,
            staffId = entity.staffId,
            serviceId = entity.serviceId,
            date = entity.date,
            startTime = entity.startTime,
            endTime = entity.endTime,
            status = entity.status,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}