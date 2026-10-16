package com.esteticapatri.app.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.AppointmentWithDetails
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.domain.CancelAppointment
import com.esteticapatri.app.domain.GetAgendaAppointments
import com.esteticapatri.app.domain.RescheduleAppointment
import com.esteticapatri.app.domain.UpdateAppointmentStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val getAgendaAppointments = GetAgendaAppointments(db.appointmentDao())
    private val cancelAppointment = CancelAppointment(db.appointmentDao())
    private val updateAppointmentStatus = UpdateAppointmentStatus(db.appointmentDao())
    private val rescheduleAppointment = RescheduleAppointment(db.appointmentDao())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val appointments: StateFlow<List<AppointmentWithDetails>> = _selectedDate.flatMapLatest { date ->
        val zone = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        getAgendaAppointments.observeDay(startOfDay, endOfDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun onNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun onToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun cancel(appointmentId: Long) {
        viewModelScope.launch {
            cancelAppointment(appointmentId).fold(
                onSuccess = { _uiMessage.value = "Turno cancelado exitosamente." },
                onFailure = { _uiMessage.value = it.message ?: "Error al cancelar el turno." }
            )
        }
    }

    fun complete(appointmentId: Long) {
        viewModelScope.launch {
            updateAppointmentStatus(appointmentId, AppointmentEntity.COMPLETED).fold(
                onSuccess = { _uiMessage.value = "Turno marcado como completado." },
                onFailure = { _uiMessage.value = it.message ?: "Error al actualizar estado." }
            )
        }
    }

    fun markNoShow(appointmentId: Long) {
        viewModelScope.launch {
            updateAppointmentStatus(appointmentId, AppointmentEntity.NO_SHOW).fold(
                onSuccess = { _uiMessage.value = "Turno marcado como no asistió." },
                onFailure = { _uiMessage.value = it.message ?: "Error al actualizar estado." }
            )
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }
}
