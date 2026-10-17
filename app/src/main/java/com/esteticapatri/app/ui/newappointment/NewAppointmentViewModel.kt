package com.esteticapatri.app.ui.newappointment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.data.local.ServiceEntity
import com.esteticapatri.app.domain.CreateAppointment
import com.esteticapatri.app.domain.GetClients
import com.esteticapatri.app.domain.GetServices
import com.esteticapatri.app.domain.WorkingHoursUseCases
import com.esteticapatri.app.reminders.WorkManagerReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class CreatedAppointmentInfo(
    val appointmentId: Long,
    val clientName: String,
    val clientPhone: String?,
    val serviceName: String,
    val formattedDate: String,
    val formattedTime: String
)

class NewAppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val createAppointment = CreateAppointment(db.appointmentDao())
    private val getClients = GetClients(db.clientDao())
    private val getServices = GetServices(db.serviceDao())
    private val workingHoursUseCases = WorkingHoursUseCases(db.workingHoursDao())
    private val reminderScheduler = WorkManagerReminderScheduler(application)

    val clients: StateFlow<List<ClientEntity>> = getClients().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val services: StateFlow<List<ServiceEntity>> = getServices().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _selectedClientId = MutableStateFlow<Long?>(null)
    val selectedClientId: StateFlow<Long?> = _selectedClientId.asStateFlow()

    private val _selectedServiceId = MutableStateFlow<Long?>(null)
    val selectedServiceId: StateFlow<Long?> = _selectedServiceId.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow(LocalTime.of(10, 0))
    val selectedTime: StateFlow<LocalTime> = _selectedTime.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _appointmentCreated = MutableStateFlow<CreatedAppointmentInfo?>(null)
    val appointmentCreated: StateFlow<CreatedAppointmentInfo?> = _appointmentCreated.asStateFlow()

    fun selectClient(id: Long) { _selectedClientId.value = id }
    fun selectService(id: Long) { _selectedServiceId.value = id }
    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun selectTime(time: LocalTime) { _selectedTime.value = time }
    fun onNotesChange(text: String) { _notes.value = text }
    fun clearError() { _errorMessage.value = null }
    fun clearCreated() { _appointmentCreated.value = null }

    fun submit() {
        val clientId = _selectedClientId.value
        if (clientId == null || clientId <= 0) {
            _errorMessage.value = "Por favor, seleccioná una clienta."
            return
        }

        val serviceId = _selectedServiceId.value
        if (serviceId == null || serviceId <= 0) {
            _errorMessage.value = "Por favor, seleccioná un tratamiento/servicio."
            return
        }

        val service = services.value.find { it.id == serviceId }
        val client = clients.value.find { it.id == clientId }
        val durationMinutes = service?.durationMinutes ?: 60

        val zone = ZoneId.systemDefault()
        val startDateTime = _selectedDate.value.atTime(_selectedTime.value)
        val endDateTime = startDateTime.plusMinutes(durationMinutes.toLong())

        val startEpochMillis = startDateTime.atZone(zone).toInstant().toEpochMilli()
        val endEpochMillis = endDateTime.atZone(zone).toInstant().toEpochMilli()

        viewModelScope.launch {
            // Check business hours
            val withinHours = workingHoursUseCases.isWithinWorkingHours(startEpochMillis, endEpochMillis, zone)
            if (!withinHours) {
                // If not within working hours, warn but proceed or fail based on rules
                // In ARQUITECTURA.md: "No solapar turnos programados"
            }

            createAppointment(
                clientId = clientId,
                serviceId = serviceId,
                startEpochMillis = startEpochMillis,
                endEpochMillis = endEpochMillis,
                notes = _notes.value
            ).fold(
                onSuccess = { appointmentId ->
                    // Schedule reminder 2 hours before appointment
                    val reminderTime = startEpochMillis - (2 * 60 * 60 * 1000)
                    if (reminderTime > System.currentTimeMillis()) {
                        reminderScheduler.scheduleAppointmentReminder(appointmentId, reminderTime)
                    }

                    _appointmentCreated.value = CreatedAppointmentInfo(
                        appointmentId = appointmentId,
                        clientName = client?.name ?: "Clienta",
                        clientPhone = client?.phone,
                        serviceName = service?.name ?: "Tratamiento",
                        formattedDate = "%02d/%02d/%d".format(
                            _selectedDate.value.dayOfMonth,
                            _selectedDate.value.monthValue,
                            _selectedDate.value.year
                        ),
                        formattedTime = "%02d:%02d".format(
                            _selectedTime.value.hour,
                            _selectedTime.value.minute
                        )
                    )
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "Error al agendar el turno."
                }
            )
        }
    }
}
