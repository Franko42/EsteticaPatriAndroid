package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.WorkingHoursDao
import com.esteticapatri.app.data.local.WorkingHoursEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class WorkingHoursUseCases(private val dao: WorkingHoursDao) {

    fun observeWorkingHours(): Flow<List<WorkingHoursEntity>> = dao.observeAll()

    suspend fun getOrInitializeDefaults(): List<WorkingHoursEntity> {
        val existing = dao.getAll()
        if (existing.isNotEmpty()) return existing

        val defaults = (1..7).map { day ->
            // 1 = Monday ... 6 = Saturday (9:00 to 19:00, 540m to 1140m). 7 = Sunday (disabled)
            WorkingHoursEntity(
                dayOfWeek = day,
                enabled = day != 7,
                startMinuteOfDay = 9 * 60,
                endMinuteOfDay = 19 * 60
            )
        }
        dao.insertAll(defaults)
        return defaults
    }

    suspend fun updateWorkingHours(hours: WorkingHoursEntity): Result<Unit> = runCatching {
        require(hours.dayOfWeek in 1..7) { "Día de la semana inválido." }
        if (hours.enabled) {
            require(hours.startMinuteOfDay in 0..1439) { "Minuto de inicio inválido." }
            require(hours.endMinuteOfDay in 1..1440) { "Minuto de fin inválido." }
            require(hours.endMinuteOfDay > hours.startMinuteOfDay) {
                "El horario de cierre debe ser posterior a la apertura."
            }
        }
        dao.insertOrUpdate(hours)
    }

    suspend fun isWithinWorkingHours(
        startEpochMillis: Long,
        endEpochMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val startZDT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startEpochMillis), zoneId)
        val endZDT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endEpochMillis), zoneId)

        // Must be same calendar day
        if (startZDT.toLocalDate() != endZDT.toLocalDate()) {
            return false
        }

        val dayOfWeek = startZDT.dayOfWeek.value // 1..7
        val config = dao.getForDay(dayOfWeek) ?: return true // If not configured, don't restrict

        if (!config.enabled) return false

        val startMinute = startZDT.hour * 60 + startZDT.minute
        val endMinute = endZDT.hour * 60 + endZDT.minute

        return startMinute >= config.startMinuteOfDay && endMinute <= config.endMinuteOfDay
    }
}
