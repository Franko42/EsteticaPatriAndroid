package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.ClientDao
import com.esteticapatri.app.data.local.ClientEntity
import kotlinx.coroutines.flow.Flow

class CreateClient(private val dao: ClientDao) {
    suspend operator fun invoke(name: String, phone: String? = null, notes: String? = null): Result<Long> = runCatching {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "El nombre no puede estar vacío." }
        val cleanPhone = phone?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedPhone = cleanPhone?.filter(Char::isDigit)

        dao.insert(
            ClientEntity(
                name = trimmedName,
                phone = cleanPhone,
                normalizedPhone = normalizedPhone,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }
}

class UpdateClient(private val dao: ClientDao) {
    suspend operator fun invoke(client: ClientEntity): Result<Unit> = runCatching {
        require(client.id > 0) { "ID de clienta inválido." }
        require(client.name.trim().isNotEmpty()) { "El nombre no puede estar vacío." }
        val normalizedPhone = client.phone?.filter(Char::isDigit)
        dao.update(client.copy(name = client.name.trim(), normalizedPhone = normalizedPhone))
    }
}

class DeactivateClient(private val dao: ClientDao) {
    suspend operator fun invoke(clientId: Long): Result<Unit> = runCatching {
        require(clientId > 0) { "ID de clienta inválido." }
        dao.deactivate(clientId)
    }
}

class GetClients(private val dao: ClientDao) {
    operator fun invoke(): Flow<List<ClientEntity>> = dao.observeActive()
    fun search(query: String): Flow<List<ClientEntity>> =
        if (query.isBlank()) dao.observeActive() else dao.searchActive(query.trim())
    suspend fun getById(id: Long): ClientEntity? = dao.getById(id)
}
