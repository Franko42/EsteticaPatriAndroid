package com.esteticapatri.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.DatabaseProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppointmentReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val appointmentId = inputData.getLong(KEY_APPOINTMENT_ID, -1L)
        if (appointmentId <= 0) return Result.failure()

        val db = DatabaseProvider.getDatabase(applicationContext)
        val appointmentWithDetails = db.appointmentDao().getByIdWithDetails(appointmentId)

        if (appointmentWithDetails != null && appointmentWithDetails.appointment.status == AppointmentEntity.SCHEDULED) {
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            val timeStr = Instant.ofEpochMilli(appointmentWithDetails.appointment.startEpochMillis)
                .atZone(ZoneId.systemDefault())
                .format(formatter)

            NotificationHelper.showAppointmentNotification(
                context = applicationContext,
                appointmentId = appointmentId,
                clientName = appointmentWithDetails.client.name,
                serviceName = appointmentWithDetails.service.name,
                formattedTime = timeStr
            )
        }

        return Result.success()
    }

    companion object {
        const val KEY_APPOINTMENT_ID = "appointment_id"
    }
}
