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

fun extractCorePhoneDigits(phone: String?): String {
    if (phone.isNullOrBlank()) return ""
    var digits = phone.filter(Char::isDigit)
    if (digits.isEmpty()) return ""
    if (digits.startsWith("54")) {
        digits = digits.removePrefix("54")
        if (digits.startsWith("9")) {
            digits = digits.removePrefix("9")
        }
    }
    if (digits.startsWith("0")) {
        digits = digits.removePrefix("0")
    }
    if (digits.length == 12) {
        val idx15 = digits.indexOf("15")
        if (idx15 in 2..4) {
            digits = digits.removeRange(idx15, idx15 + 2)
        }
    } else if (digits.length in 9..10 && digits.startsWith("15")) {
        digits = digits.removePrefix("15")
    }
    return digits
}

fun arePhonesMatching(p1: String?, p2: String?): Boolean {
    if (p1.isNullOrBlank() || p2.isNullOrBlank()) return false
    val raw1 = p1.filter(Char::isDigit)
    val raw2 = p2.filter(Char::isDigit)
    if (raw1.isEmpty() || raw2.isEmpty()) return false
    if (raw1 == raw2) return true

    val core1 = extractCorePhoneDigits(p1)
    val core2 = extractCorePhoneDigits(p2)
    if (core1.isNotEmpty() && core2.isNotEmpty()) {
        if (core1 == core2) return true
        val minCoreLen = minOf(core1.length, core2.length)
        if (minCoreLen >= 7 && (core1.endsWith(core2) || core2.endsWith(core1))) return true
    }

    val minRawLen = minOf(raw1.length, raw2.length)
    return minRawLen >= 8 && (raw1.endsWith(raw2) || raw2.endsWith(raw1))
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

