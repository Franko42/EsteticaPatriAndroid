package com.esteticapatri.app.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: EsteticaDatabase? = null

    fun getDatabase(context: Context): EsteticaDatabase {
        return instance ?: synchronized(this) {
            val created = Room.databaseBuilder(
                context.applicationContext,
                EsteticaDatabase::class.java,
                "estetica_patri.db"
            ).build()
            instance = created
            created
        }
    }
}
