package com.esteticapatri.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppointmentDao {
    @Query("SELECT * FROM appointments WHERE startEpochMillis >= :from AND startEpochMillis < :until ORDER BY startEpochMillis")
    abstract fun observeBetween(from: Long, until: Long): Flow<List<AppointmentEntity>>

    @Transaction
    @Query("SELECT * FROM appointments WHERE startEpochMillis >= :from AND startEpochMillis < :until ORDER BY startEpochMillis")
    abstract fun observeWithDetailsBetween(from: Long, until: Long): Flow<List<AppointmentWithDetails>>

    @Transaction
    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    abstract suspend fun getByIdWithDetails(id: Long): AppointmentWithDetails?

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    abstract suspend fun getById(id: Long): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE status = 'SCHEDULED' AND startEpochMillis >= :from ORDER BY startEpochMillis ASC")
    abstract suspend fun getUpcomingScheduled(from: Long): List<AppointmentEntity>

    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'SCHEDULED' AND startEpochMillis < :candidateEnd AND endEpochMillis > :candidateStart")
    abstract suspend fun countOverlaps(candidateStart: Long, candidateEnd: Long): Int

    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'SCHEDULED' AND id != :excludeId AND startEpochMillis < :candidateEnd AND endEpochMillis > :candidateStart")
    abstract suspend fun countOverlapsExcluding(candidateStart: Long, candidateEnd: Long, excludeId: Long): Int

    @Insert
    abstract suspend fun insert(appointment: AppointmentEntity): Long

    @Transaction
    open suspend fun insertIfAvailable(appointment: AppointmentEntity): Long {
        require(appointment.endEpochMillis > appointment.startEpochMillis) {
            "La hora de fin debe ser posterior al inicio."
        }
        check(countOverlaps(appointment.startEpochMillis, appointment.endEpochMillis) == 0) {
            "El horario se superpone con otro turno."
        }
        return insert(appointment)
    }

    @Update
    abstract suspend fun updateRaw(appointment: AppointmentEntity)

    @Transaction
    open suspend fun reschedule(id: Long, newStart: Long, newEnd: Long, updatedAt: Long = System.currentTimeMillis()) {
        require(newEnd > newStart) { "La hora de fin debe ser posterior al inicio." }
        val current = getById(id) ?: error("No existe el turno con ID $id")
        check(current.status == AppointmentEntity.SCHEDULED) { "Solo se pueden reprogramar turnos en estado SCHEDULED." }
        check(countOverlapsExcluding(newStart, newEnd, id) == 0) { "El horario se superpone con otro turno." }
        updateRaw(current.copy(startEpochMillis = newStart, endEpochMillis = newEnd, updatedAtEpochMillis = updatedAt))
    }

    @Query("UPDATE appointments SET status = :status, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    abstract suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE appointments SET status = 'CANCELLED', updatedAtEpochMillis = :updatedAt WHERE id = :id AND status = 'SCHEDULED'")
    abstract suspend fun cancelScheduled(id: Long, updatedAt: Long = System.currentTimeMillis()): Int
}
