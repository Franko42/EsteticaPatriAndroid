package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.WorkingHoursDao
import com.esteticapatri.app.data.local.WorkingHoursEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class FakeWorkingHoursDao : WorkingHoursDao {
    val items = mutableListOf<WorkingHoursEntity>()

    override fun observeAll(): Flow<List<WorkingHoursEntity>> = flowOf(items)
    override suspend fun getAll(): List<WorkingHoursEntity> = items.toList()
    override suspend fun getForDay(dayOfWeek: Int): WorkingHoursEntity? = items.find { it.dayOfWeek == dayOfWeek }
    override suspend fun insertAll(hours: List<WorkingHoursEntity>) {
        items.clear()
        items.addAll(hours)
    }
    override suspend fun insertOrUpdate(hours: WorkingHoursEntity) {
        val idx = items.indexOfFirst { it.dayOfWeek == hours.dayOfWeek }
        if (idx >= 0) items[idx] = hours else items.add(hours)
    }
    override suspend fun update(hours: WorkingHoursEntity) {
        insertOrUpdate(hours)
    }
}

class WorkingHoursValidationTest {

    @Test
    fun rejectsInvalidWorkingHours() = runBlocking {
        val dao = FakeWorkingHoursDao()
        val useCases = WorkingHoursUseCases(dao)

        // Day of week < 1 or > 7
        val badDay = useCases.updateWorkingHours(WorkingHoursEntity(0, true, 540, 1140))
        assertTrue(badDay.isFailure)

        // End minute <= start minute
        val badTime = useCases.updateWorkingHours(WorkingHoursEntity(1, true, 600, 500))
        assertTrue(badTime.isFailure)

        // Valid update succeeds
        val valid = useCases.updateWorkingHours(WorkingHoursEntity(1, true, 9 * 60, 18 * 60))
        assertTrue(valid.isSuccess)
    }

    @Test
    fun validatesAppointmentWithinBusinessHours() = runBlocking {
        val dao = FakeWorkingHoursDao()
        val useCases = WorkingHoursUseCases(dao)

        // Initialize defaults (Mon-Sat 09:00 to 19:00, Sun closed)
        useCases.getOrInitializeDefaults()

        val zone = ZoneId.systemDefault()
        // Next Monday
        val monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))

        // 10:00 to 11:00 on Monday -> within hours
        val startMilli = monday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val endMilli = monday.atTime(11, 0).atZone(zone).toInstant().toEpochMilli()
        assertTrue(useCases.isWithinWorkingHours(startMilli, endMilli, zone))

        // 07:00 to 08:00 on Monday (before opening at 09:00) -> outside hours
        val earlyStart = monday.atTime(7, 0).atZone(zone).toInstant().toEpochMilli()
        val earlyEnd = monday.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        assertFalse(useCases.isWithinWorkingHours(earlyStart, earlyEnd, zone))

        // Sunday appointment -> outside hours (Sunday disabled)
        val sunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        val sunStart = sunday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val sunEnd = sunday.atTime(11, 0).atZone(zone).toInstant().toEpochMilli()
        assertFalse(useCases.isWithinWorkingHours(sunStart, sunEnd, zone))
    }

    @Test
    fun allowsUpdatingAndRespectsCustomizedWorkingHours() = runBlocking {
        val dao = FakeWorkingHoursDao()
        val useCases = WorkingHoursUseCases(dao)

        useCases.getOrInitializeDefaults()

        // Configure Tuesday to be 14:00 to 20:00 (840m to 1200m)
        val result = useCases.updateWorkingHours(
            WorkingHoursEntity(
                dayOfWeek = 2, // Tuesday
                enabled = true,
                startMinuteOfDay = 14 * 60,
                endMinuteOfDay = 20 * 60
            )
        )
        assertTrue(result.isSuccess)

        val zone = ZoneId.systemDefault()
        val tuesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY))

        // 10:00 to 11:00 on Tuesday -> now outside hours (opens at 14:00)
        val morningStart = tuesday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val morningEnd = tuesday.atTime(11, 0).atZone(zone).toInstant().toEpochMilli()
        assertFalse(useCases.isWithinWorkingHours(morningStart, morningEnd, zone))

        // 16:00 to 17:00 on Tuesday -> within customized hours
        val afternoonStart = tuesday.atTime(16, 0).atZone(zone).toInstant().toEpochMilli()
        val afternoonEnd = tuesday.atTime(17, 0).atZone(zone).toInstant().toEpochMilli()
        assertTrue(useCases.isWithinWorkingHours(afternoonStart, afternoonEnd, zone))
    }
}
