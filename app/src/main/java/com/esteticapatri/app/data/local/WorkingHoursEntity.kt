package com.esteticapatri.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "working_hours")
data class WorkingHoursEntity(
    @PrimaryKey val dayOfWeek: Int,
    val enabled: Boolean,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int
)
