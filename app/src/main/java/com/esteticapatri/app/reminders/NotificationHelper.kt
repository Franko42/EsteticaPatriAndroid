package com.esteticapatri.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.esteticapatri.app.MainActivity

object NotificationHelper {
    const val CHANNEL_ID = "recordatorios_turnos"
    private const val CHANNEL_NAME = "Recordatorios de turnos"
    private const val CHANNEL_DESC = "Avisos previos para citas y turnos agendados"

    const val BACKUP_CHANNEL_ID = "copias_seguridad"
    private const val BACKUP_CHANNEL_NAME = "Copias de seguridad"
    private const val BACKUP_CHANNEL_DESC = "Avisos de copias de seguridad automáticas"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val appointmentChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }

            val backupChannel = NotificationChannel(
                BACKUP_CHANNEL_ID,
                BACKUP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = BACKUP_CHANNEL_DESC
            }

            notificationManager.createNotificationChannel(appointmentChannel)
            notificationManager.createNotificationChannel(backupChannel)
        }
    }

    fun showAppointmentNotification(
        context: Context,
        appointmentId: Long,
        clientName: String,
        serviceName: String,
        formattedTime: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_APPOINTMENT_ID", appointmentId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio de turno: $clientName")
            .setContentText("$serviceName a las $formattedTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(appointmentId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted by user yet
        }
    }

    fun showBackupNotification(
        context: Context,
        backupFileName: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            8888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, BACKUP_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Copia de seguridad realizada")
            .setContentText("Respaldo diario automático guardado con éxito.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Se creó el respaldo diario automático de turnos y clientas ($backupFileName). Se conservan las últimas 3 copias.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(8888, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted by user yet
        }
    }
}
