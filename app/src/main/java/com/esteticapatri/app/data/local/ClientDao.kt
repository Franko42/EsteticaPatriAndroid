package com.esteticapatri.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE active = 1 ORDER BY name COLLATE NOCASE")
    fun observeActive(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE active = 1 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name COLLATE NOCASE")
    fun searchActive(query: String): Flow<List<ClientEntity>>

    @Insert suspend fun insert(client: ClientEntity): Long
    @Update suspend fun update(client: ClientEntity)
    @Query("UPDATE clients SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}
