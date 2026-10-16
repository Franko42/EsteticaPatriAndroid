package com.esteticapatri.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val priceMinorUnits: Long? = null,
    val notes: String? = null,
    val active: Boolean = true
)
