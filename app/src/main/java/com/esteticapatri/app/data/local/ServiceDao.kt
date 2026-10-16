package com.esteticapatri.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE active = 1 ORDER BY name COLLATE NOCASE")
    fun observeActive(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ServiceEntity?

    @Insert suspend fun insert(service: ServiceEntity): Long
    @Update suspend fun update(service: ServiceEntity)
    @Query("UPDATE services SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}
