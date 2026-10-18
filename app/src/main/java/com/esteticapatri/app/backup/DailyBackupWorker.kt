package com.esteticapatri.app.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.reminders.NotificationHelper
import java.time.LocalDate

class DailyBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = LocalDate.now().toString()
        val lastBackupDate = prefs.getString(KEY_LAST_BACKUP_DATE, null)

        // Si ya se realizó la copia automática hoy, no duplicar
        if (lastBackupDate == todayStr) {
            return Result.success()
        }

        val db = DatabaseProvider.getDatabase(applicationContext)
        val backupManager = BackupManager(applicationContext, db)

        val result = backupManager.createLocalBackup(password = null)
        return result.fold(
            onSuccess = { file ->
                prefs.edit().putString(KEY_LAST_BACKUP_DATE, todayStr).apply()
                NotificationHelper.showBackupNotification(
                    context = applicationContext,
                    backupFileName = file.name
                )
                Result.success()
            },
            onFailure = {
                Result.retry()
            }
        )
    }

    companion object {
        const val PREFS_NAME = "backup_prefs"
        const val KEY_LAST_BACKUP_DATE = "last_auto_backup_date"
        const val WORK_NAME = "daily_auto_backup"
    }
}
