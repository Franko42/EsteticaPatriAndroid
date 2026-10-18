package com.esteticapatri.app.backup

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.reminders.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object DailyBackupScheduler {

    fun scheduleDailyBackup(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailyBackupWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    suspend fun checkAndRunDailyBackupNow(context: Context) {
        val prefs = context.getSharedPreferences(DailyBackupWorker.PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = LocalDate.now().toString()
        val lastBackupDate = prefs.getString(DailyBackupWorker.KEY_LAST_BACKUP_DATE, null)

        if (lastBackupDate != todayStr) {
            withContext(Dispatchers.IO) {
                val db = DatabaseProvider.getDatabase(context)
                val backupManager = BackupManager(context, db)
                backupManager.createLocalBackup(password = null).onSuccess { file ->
                    prefs.edit().putString(DailyBackupWorker.KEY_LAST_BACKUP_DATE, todayStr).apply()
                    NotificationHelper.showBackupNotification(context, file.name)
                }
            }
        }
    }
}
