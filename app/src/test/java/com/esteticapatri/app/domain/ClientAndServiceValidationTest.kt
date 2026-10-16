package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.ClientDao
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.data.local.ServiceDao
import com.esteticapatri.app.data.local.ServiceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeClientDao : ClientDao {
    val items = mutableListOf<ClientEntity>()
    private var nextId = 1L

    override fun observeActive(): Flow<List<ClientEntity>> = flowOf(items.filter { it.active })
    override suspend fun getById(id: Long): ClientEntity? = items.find { it.id == id }
    override fun searchActive(query: String): Flow<List<ClientEntity>> = flowOf(
        items.filter { it.active && (it.name.contains(query, ignoreCase = true) || (it.phone?.contains(query) == true)) }
    )
    override suspend fun insert(client: ClientEntity): Long {
        val assigned = client.copy(id = nextId++)
        items.add(assigned)
        return assigned.id
    }
    override suspend fun update(client: ClientEntity) {
        val idx = items.indexOfFirst { it.id == client.id }
        if (idx >= 0) items[idx] = client
    }
    override suspend fun deactivate(id: Long) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) items[idx] = items[idx].copy(active = false)
    }
}

class FakeServiceDao : ServiceDao {
    val items = mutableListOf<ServiceEntity>()
    private var nextId = 1L

    override fun observeActive(): Flow<List<ServiceEntity>> = flowOf(items.filter { it.active })
    override suspend fun getById(id: Long): ServiceEntity? = items.find { it.id == id }
    override suspend fun insert(service: ServiceEntity): Long {
        val assigned = service.copy(id = nextId++)
        items.add(assigned)
        return assigned.id
    }
    override suspend fun update(service: ServiceEntity) {
        val idx = items.indexOfFirst { it.id == service.id }
        if (idx >= 0) items[idx] = service
    }
    override suspend fun deactivate(id: Long) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) items[idx] = items[idx].copy(active = false)
    }
}

class ClientAndServiceValidationTest {

    @Test
    fun clientValidationRules() = runBlocking {
        val clientDao = FakeClientDao()
        val createClient = CreateClient(clientDao)

        // Blank name rejected
        val blankRes = createClient("   ", "5493764123456")
        assertTrue(blankRes.isFailure)

        // Normal phone normalized
        val okRes = createClient("Laura Gómez", "+54 9 (3764) 12-3456", "Clienta frecuente")
        assertTrue(okRes.isSuccess)

        val inserted = clientDao.items.first()
        assertEquals("Laura Gómez", inserted.name)
        assertEquals("5493764123456", inserted.normalizedPhone)
    }

    @Test
    fun serviceValidationRules() = runBlocking {
        val serviceDao = FakeServiceDao()
        val createService = CreateService(serviceDao)

        // Blank name rejected
        assertTrue(createService("   ", 30).isFailure)

        // Duration below 5 min rejected
        assertTrue(createService("Express", 4).isFailure)

        // Duration above 480 min (8 hours) rejected
        assertTrue(createService("Mega Day", 500).isFailure)

        // Negative price rejected
        assertTrue(createService("Corte", 30, priceMinorUnits = -100).isFailure)

        // Valid service accepted
        val ok = createService("Limpieza Facial", 60, priceMinorUnits = 850000)
        assertTrue(ok.isSuccess)
    }
}
