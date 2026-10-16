package com.esteticapatri.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esteticapatri.app.data.local.DatabaseProvider

class RescheduleRemindersWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)
        val scheduler = WorkManagerReminderScheduler(applicationContext)
        val now = System.currentTimeMillis()

        val upcoming = db.appointmentDao().getUpcomingScheduled(now)
        for (appointment in upcoming) {
            // Schedule reminder for 2 hours before the appointment, or right now if less than 2 hours away
            val reminderTime = appointment.startEpochMillis - (2 * 60 * 60 * 1000)
            if (reminderTime > now) {
                scheduler.scheduleAppointmentReminder(appointment.id, reminderTime)
            }
        }

        return Result.success()
    }
}
