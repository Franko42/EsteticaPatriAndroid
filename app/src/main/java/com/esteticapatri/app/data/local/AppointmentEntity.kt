package com.esteticapatri.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(entity = ClientEntity::class, parentColumns = ["id"], childColumns = ["clientId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ServiceEntity::class, parentColumns = ["id"], childColumns = ["serviceId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("clientId"), Index("serviceId"), Index("startEpochMillis")]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val serviceId: Long,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val status: String = SCHEDULED,
    val notes: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val SCHEDULED = "SCHEDULED"
        const val COMPLETED = "COMPLETED"
        const val CANCELLED = "CANCELLED"
        const val NO_SHOW = "NO_SHOW"
    }
}
