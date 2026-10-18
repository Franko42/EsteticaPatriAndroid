package com.esteticapatri.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = WorkManagerReminderScheduler(context)
            scheduler.rescheduleAllPendingReminders()
            com.esteticapatri.app.backup.DailyBackupScheduler.scheduleDailyBackup(context)
        }
    }
}
