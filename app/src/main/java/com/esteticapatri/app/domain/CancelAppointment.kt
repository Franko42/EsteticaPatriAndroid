package com.esteticapatri.app.domain

import com.esteticapatri.app.data.local.AppointmentDao

class CancelAppointment(private val dao: AppointmentDao) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        require(id > 0) { "El turno no es válido." }
        check(dao.cancelScheduled(id) == 1) { "No hay un turno pendiente para cancelar con ese ID." }
    }
}
