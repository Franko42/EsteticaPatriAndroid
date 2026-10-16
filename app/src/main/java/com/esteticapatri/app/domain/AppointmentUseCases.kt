package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.AppointmentDao
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow

class RescheduleAppointment(private val dao: AppointmentDao) {
    suspend operator fun invoke(id: Long, newStartEpochMillis: Long, newEndEpochMillis: Long): Result<Unit> = runCatching {
        require(id > 0) { "ID de turno inválido." }
        require(newEndEpochMillis > newStartEpochMillis) { "La hora de fin debe ser posterior al inicio." }
        dao.reschedule(id, newStartEpochMillis, newEndEpochMillis)
    }
}

class UpdateAppointmentStatus(private val dao: AppointmentDao) {
    suspend operator fun invoke(id: Long, newStatus: String): Result<Unit> = runCatching {
        require(id > 0) { "ID de turno inválido." }
        require(
            newStatus in listOf(
                AppointmentEntity.SCHEDULED,
                AppointmentEntity.COMPLETED,
                AppointmentEntity.CANCELLED,
                AppointmentEntity.NO_SHOW
            )
        ) { "Estado de turno inválido: $newStatus" }

        val rowsUpdated = dao.updateStatus(id, newStatus)
        check(rowsUpdated == 1) { "No se encontró el turno para actualizar." }
    }
}

class GetAgendaAppointments(private val dao: AppointmentDao) {
    fun observeDay(startOfDayEpochMillis: Long, endOfDayEpochMillis: Long): Flow<List<AppointmentWithDetails>> {
        return dao.observeWithDetailsBetween(startOfDayEpochMillis, endOfDayEpochMillis)
    }

    suspend fun getUpcomingScheduled(fromEpochMillis: Long): List<AppointmentEntity> {
        return dao.getUpcomingScheduled(fromEpochMillis)
    }

    suspend fun getByIdWithDetails(id: Long): AppointmentWithDetails? {
        return dao.getByIdWithDetails(id)
    }
}
