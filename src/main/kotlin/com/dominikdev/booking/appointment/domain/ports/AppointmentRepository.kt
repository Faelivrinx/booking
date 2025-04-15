package com.dominikdev.booking.appointment.domain.ports

import com.dominikdev.booking.appointment.domain.model.Appointment
import java.time.LocalDateTime
import java.util.*

interface AppointmentRepository {
    fun save(appointment: Appointment): Appointment
    fun findById(id: UUID): Appointment?
    fun findByClientId(clientId: UUID): List<Appointment>
    fun findByStaffId(staffId: UUID): List<Appointment>
    fun findOverlappingAppointments(
        staffId: UUID,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<Appointment>
    fun findByBusinessIdAndDateRange(
        businessId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Appointment>
}