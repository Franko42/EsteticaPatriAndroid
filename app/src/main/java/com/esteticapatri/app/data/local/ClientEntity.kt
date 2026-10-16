package com.esteticapatri.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clients", indices = [Index("normalizedPhone")])
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val normalizedPhone: String? = null,
    val notes: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val active: Boolean = true
)
