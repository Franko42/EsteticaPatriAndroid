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

data class ClientImportCandidate(
    val name: String,
    val phone: String? = null,
    val notes: String? = null
)

fun arePhonesMatching(p1: String?, p2: String?): Boolean {
    if (p1.isNullOrBlank() || p2.isNullOrBlank()) return false
    val digits1 = p1.filter(Char::isDigit)
    val digits2 = p2.filter(Char::isDigit)
    if (digits1.isEmpty() || digits2.isEmpty()) return false
    if (digits1 == digits2) return true
    val minLen = minOf(digits1.length, digits2.length)
    return minLen >= 8 && (digits1.endsWith(digits2) || digits2.endsWith(digits1))
}

class ImportClients(private val dao: ClientDao) {
    suspend operator fun invoke(candidates: List<ClientImportCandidate>): Result<Int> = runCatching {
        if (candidates.isEmpty()) return@runCatching 0

        val existing = dao.getAllActive()
        val existingNormalizedPhones = existing.mapNotNull { it.normalizedPhone }.toSet()
        val existingNames = existing.map { it.name.trim().lowercase() }.toSet()

        val entitiesToInsert = mutableListOf<ClientEntity>()
        val seenPhonesInBatch = mutableSetOf<String>()
        val seenNamesInBatch = mutableSetOf<String>()

        for (candidate in candidates) {
            val trimmedName = candidate.name.trim()
            if (trimmedName.isEmpty()) continue

            val cleanPhone = candidate.phone?.trim()?.takeIf { it.isNotEmpty() }
            val normalizedPhone = cleanPhone?.filter(Char::isDigit)

            val nameKey = trimmedName.lowercase()
            val phoneKey = normalizedPhone?.takeIf { it.isNotEmpty() }

            val alreadyInDb = (phoneKey != null && existingNormalizedPhones.any { arePhonesMatching(it, phoneKey) }) ||
                existingNames.contains(nameKey)
            val alreadyInBatch = (phoneKey != null && seenPhonesInBatch.any { arePhonesMatching(it, phoneKey) }) ||
                seenNamesInBatch.contains(nameKey)

            if (!alreadyInDb && !alreadyInBatch) {
                if (phoneKey != null) seenPhonesInBatch.add(phoneKey)
                seenNamesInBatch.add(nameKey)

                entitiesToInsert.add(
                    ClientEntity(
                        name = trimmedName,
                        phone = cleanPhone,
                        normalizedPhone = normalizedPhone,
                        notes = candidate.notes?.trim()?.takeIf { it.isNotEmpty() }
                    )
                )
            }
        }

        if (entitiesToInsert.isNotEmpty()) {
            dao.insertAll(entitiesToInsert)
        }

        entitiesToInsert.size
    }
}

