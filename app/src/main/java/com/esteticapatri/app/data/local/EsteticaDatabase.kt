package com.esteticapatri.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClientEntity::class, ServiceEntity::class, AppointmentEntity::class, WorkingHoursEntity::class],
    version = 1,
    exportSchema = true
)
abstract class EsteticaDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun workingHoursDao(): WorkingHoursDao
}
