package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.AppointmentDao
import com.esteticapatri.app.data.local.AppointmentEntity

class CreateAppointment(private val dao: AppointmentDao) {
    suspend operator fun invoke(
        clientId: Long,
        serviceId: Long,
        startEpochMillis: Long,
        endEpochMillis: Long,
        notes: String? = null
    ): Result<Long> = runCatching {
        require(clientId > 0) { "Seleccioná una clienta válida." }
        require(serviceId > 0) { "Seleccioná un servicio válido." }
        require(endEpochMillis > startEpochMillis) { "La duración no es válida." }
        dao.insertIfAvailable(
            AppointmentEntity(
                clientId = clientId,
                serviceId = serviceId,
                startEpochMillis = startEpochMillis,
                endEpochMillis = endEpochMillis,
                notes = notes?.trim()?.takeIf(String::isNotEmpty)
            )
        )
    }
}
