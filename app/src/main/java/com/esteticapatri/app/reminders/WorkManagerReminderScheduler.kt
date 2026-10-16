package com.esteticapatri.app.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleAppointmentReminder(appointmentId: Long, triggerAtEpochMillis: Long) {
        val delayMillis = maxOf(0L, triggerAtEpochMillis - System.currentTimeMillis())

        val workRequest = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(AppointmentReminderWorker.KEY_APPOINTMENT_ID to appointmentId))
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(appointmentId),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun cancelAppointmentReminder(appointmentId: Long) {
        workManager.cancelUniqueWork(uniqueWorkName(appointmentId))
    }

    override fun rescheduleAllPendingReminders() {
        val workRequest = OneTimeWorkRequestBuilder<RescheduleRemindersWorker>().build()
        workManager.enqueueUniqueWork(
            "reschedule_all_reminders",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun uniqueWorkName(appointmentId: Long): String = "appointment_reminder_$appointmentId"
}
