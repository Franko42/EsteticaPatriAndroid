package com.esteticapatri.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class AppointmentWithDetails(
    @Embedded val appointment: AppointmentEntity,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: ClientEntity,
    @Relation(
        parentColumn = "serviceId",
        entityColumn = "id"
    )
    val service: ServiceEntity
)
