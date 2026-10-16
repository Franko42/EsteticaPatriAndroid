package com.esteticapatri.app.ui.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.data.local.ServiceEntity
import com.esteticapatri.app.domain.CreateService
import com.esteticapatri.app.domain.DeactivateService
import com.esteticapatri.app.domain.GetServices
import com.esteticapatri.app.domain.UpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServicesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val getServices = GetServices(db.serviceDao())
    private val createService = CreateService(db.serviceDao())
    private val updateService = UpdateService(db.serviceDao())
    private val deactivateService = DeactivateService(db.serviceDao())

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    val services: StateFlow<List<ServiceEntity>> = getServices().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addService(name: String, durationMinutes: Int, priceMinorUnits: Long?, notes: String?) {
        viewModelScope.launch {
            createService(name, durationMinutes, priceMinorUnits, notes).fold(
                onSuccess = { _uiMessage.value = "Servicio guardado exitosamente." },
                onFailure = { _uiMessage.value = it.message ?: "Error al guardar servicio." }
            )
        }
    }

    fun editService(service: ServiceEntity) {
        viewModelScope.launch {
            updateService(service).fold(
                onSuccess = { _uiMessage.value = "Servicio actualizado exitosamente." },
                onFailure = { _uiMessage.value = it.message ?: "Error al actualizar servicio." }
            )
        }
    }

    fun removeService(id: Long) {
        viewModelScope.launch {
            deactivateService(id).fold(
                onSuccess = { _uiMessage.value = "Servicio desactivado." },
                onFailure = { _uiMessage.value = it.message ?: "Error al desactivar servicio." }
            )
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }
}
