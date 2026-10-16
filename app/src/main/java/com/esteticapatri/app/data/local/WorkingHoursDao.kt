package com.esteticapatri.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkingHoursDao {
    @Query("SELECT * FROM working_hours ORDER BY dayOfWeek ASC")
    fun observeAll(): Flow<List<WorkingHoursEntity>>

    @Query("SELECT * FROM working_hours ORDER BY dayOfWeek ASC")
    suspend fun getAll(): List<WorkingHoursEntity>

    @Query("SELECT * FROM working_hours WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getForDay(dayOfWeek: Int): WorkingHoursEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hours: List<WorkingHoursEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(hours: WorkingHoursEntity)

    @Update
    suspend fun update(hours: WorkingHoursEntity)
}
