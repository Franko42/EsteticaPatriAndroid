package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.ServiceDao
import com.esteticapatri.app.data.local.ServiceEntity
import kotlinx.coroutines.flow.Flow

class CreateService(private val dao: ServiceDao) {
    suspend operator fun invoke(
        name: String,
        durationMinutes: Int,
        priceMinorUnits: Long? = null,
        notes: String? = null
    ): Result<Long> = runCatching {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "El nombre del servicio no puede estar vacío." }
        require(durationMinutes in 5..480) { "La duración debe ser entre 5 y 480 minutos." }
        if (priceMinorUnits != null) {
            require(priceMinorUnits >= 0) { "El precio no puede ser negativo." }
        }

        dao.insert(
            ServiceEntity(
                name = trimmedName,
                durationMinutes = durationMinutes,
                priceMinorUnits = priceMinorUnits,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }
}

class UpdateService(private val dao: ServiceDao) {
    suspend operator fun invoke(service: ServiceEntity): Result<Unit> = runCatching {
        require(service.id > 0) { "ID de servicio inválido." }
        require(service.name.trim().isNotEmpty()) { "El nombre del servicio no puede estar vacío." }
        require(service.durationMinutes in 5..480) { "La duración debe ser entre 5 y 480 minutos." }
        if (service.priceMinorUnits != null) {
            require(service.priceMinorUnits >= 0) { "El precio no puede ser negativo." }
        }
        dao.update(service.copy(name = service.name.trim(), notes = service.notes?.trim()?.takeIf { it.isNotEmpty() }))
    }
}

class DeactivateService(private val dao: ServiceDao) {
    suspend operator fun invoke(serviceId: Long): Result<Unit> = runCatching {
        require(serviceId > 0) { "ID de servicio inválido." }
        dao.deactivate(serviceId)
    }
}

class GetServices(private val dao: ServiceDao) {
    operator fun invoke(): Flow<List<ServiceEntity>> = dao.observeActive()
    suspend fun getById(id: Long): ServiceEntity? = dao.getById(id)
}
