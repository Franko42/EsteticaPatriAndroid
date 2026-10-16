package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.AppointmentDao
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAppointmentDao : AppointmentDao() {
    val items = mutableListOf<AppointmentEntity>()
    private var nextId = 1L

    override fun observeBetween(from: Long, until: Long): Flow<List<AppointmentEntity>> {
        return flowOf(items.filter { it.startEpochMillis in from until until })
    }

    override fun observeWithDetailsBetween(from: Long, until: Long): Flow<List<AppointmentWithDetails>> {
        return flowOf(emptyList())
    }

    override suspend fun getByIdWithDetails(id: Long): AppointmentWithDetails? = null

    override suspend fun getById(id: Long): AppointmentEntity? {
        return items.find { it.id == id }
    }

    override suspend fun getUpcomingScheduled(from: Long): List<AppointmentEntity> {
        return items.filter { it.status == AppointmentEntity.SCHEDULED && it.startEpochMillis >= from }
    }

    override suspend fun countOverlaps(candidateStart: Long, candidateEnd: Long): Int {
        return items.count {
            it.status == AppointmentEntity.SCHEDULED &&
                it.startEpochMillis < candidateEnd &&
                it.endEpochMillis > candidateStart
        }
    }

    override suspend fun countOverlapsExcluding(candidateStart: Long, candidateEnd: Long, excludeId: Long): Int {
        return items.count {
            it.id != excludeId &&
                it.status == AppointmentEntity.SCHEDULED &&
                it.startEpochMillis < candidateEnd &&
                it.endEpochMillis > candidateStart
        }
    }

    override suspend fun insert(appointment: AppointmentEntity): Long {
        val assigned = appointment.copy(id = nextId++)
        items.add(assigned)
        return assigned.id
    }

    override suspend fun updateRaw(appointment: AppointmentEntity) {
        val index = items.indexOfFirst { it.id == appointment.id }
        if (index >= 0) {
            items[index] = appointment
        }
    }

    override suspend fun updateStatus(id: Long, status: String, updatedAt: Long): Int {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) {
            items[index] = items[index].copy(status = status, updatedAtEpochMillis = updatedAt)
            return 1
        }
        return 0
    }

    override suspend fun cancelScheduled(id: Long, updatedAt: Long): Int {
        val index = items.indexOfFirst { it.id == id && it.status == AppointmentEntity.SCHEDULED }
        if (index >= 0) {
            items[index] = items[index].copy(status = AppointmentEntity.CANCELLED, updatedAtEpochMillis = updatedAt)
            return 1
        }
        return 0
    }
}

class AppointmentValidationTest {

    private lateinit var dao: FakeAppointmentDao
    private lateinit var createAppointment: CreateAppointment
    private lateinit var cancelAppointment: CancelAppointment
    private lateinit var rescheduleAppointment: RescheduleAppointment

    @Before
    fun setUp() {
        dao = FakeAppointmentDao()
        createAppointment = CreateAppointment(dao)
        cancelAppointment = CancelAppointment(dao)
        rescheduleAppointment = RescheduleAppointment(dao)
    }

    @Test
    fun rejectsEndEqualOrBeforeStart() = runBlocking {
        val resultEqual = createAppointment(
            clientId = 1L,
            serviceId = 1L,
            startEpochMillis = 1000L,
            endEpochMillis = 1000L
        )
        assertTrue(resultEqual.isFailure)

        val resultBefore = createAppointment(
            clientId = 1L,
            serviceId = 1L,
            startEpochMillis = 2000L,
            endEpochMillis = 1000L
        )
        assertTrue(resultBefore.isFailure)
    }

    @Test
    fun rejectsOverlapsAndAllowsAdjacent() = runBlocking {
        // Create 10:00 to 11:00 (1000 to 2000)
        val firstResult = createAppointment(
            clientId = 1L,
            serviceId = 1L,
            startEpochMillis = 1000L,
            endEpochMillis = 2000L
        )
        assertTrue(firstResult.isSuccess)

        // Exact overlap (1000 to 2000) -> should fail
        val exactOverlap = createAppointment(1L, 1L, 1000L, 2000L)
        assertTrue(exactOverlap.isFailure)

        // Partial overlap starts inside (1500 to 2500) -> should fail
        val partialOverlap = createAppointment(1L, 1L, 1500L, 2500L)
        assertTrue(partialOverlap.isFailure)

        // Contained overlap (1200 to 1800) -> should fail
        val containedOverlap = createAppointment(1L, 1L, 1200L, 1800L)
        assertTrue(containedOverlap.isFailure)

        // Adjacent / consecutive appointment (2000 to 3000) -> MUST succeed
        val adjacentAfter = createAppointment(1L, 1L, 2000L, 3000L)
        assertTrue(adjacentAfter.isSuccess)

        // Adjacent before (500 to 1000) -> MUST succeed
        val adjacentBefore = createAppointment(1L, 1L, 500L, 1000L)
        assertTrue(adjacentBefore.isSuccess)
    }

    @Test
    fun cancelledAppointmentsDoNotBlockSlots() = runBlocking {
        val res = createAppointment(1L, 1L, 1000L, 2000L)
        val id = res.getOrThrow()

        // Cancel the appointment
        val cancelRes = cancelAppointment(id)
        assertTrue(cancelRes.isSuccess)

        // Now creating appointment in the exact same slot MUST succeed
        val newRes = createAppointment(2L, 1L, 1000L, 2000L)
        assertTrue(newRes.isSuccess)
    }

    @Test
    fun rejectsDoubleCancellation() = runBlocking {
        val id = createAppointment(1L, 1L, 1000L, 2000L).getOrThrow()
        assertTrue(cancelAppointment(id).isSuccess)

        // Second cancellation must fail
        val secondCancel = cancelAppointment(id)
        assertTrue(secondCancel.isFailure)
    }

    @Test
    fun rescheduleChecksOverlaps() = runBlocking {
        val id1 = createAppointment(1L, 1L, 1000L, 2000L).getOrThrow()
        val id2 = createAppointment(2L, 1L, 3000L, 4000L).getOrThrow()

        // Reschedule id2 to collide with id1 -> must fail
        val collide = rescheduleAppointment(id2, 1500L, 2500L)
        assertTrue(collide.isFailure)

        // Reschedule id2 to free slot (5000 to 6000) -> must succeed
        val ok = rescheduleAppointment(id2, 5000L, 6000L)
        assertTrue(ok.isSuccess)
    }
}
