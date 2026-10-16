package com.esteticapatri.app.reminders

interface ReminderScheduler {
    fun scheduleAppointmentReminder(appointmentId: Long, triggerAtEpochMillis: Long)
    fun cancelAppointmentReminder(appointmentId: Long)
    fun rescheduleAllPendingReminders()
}
