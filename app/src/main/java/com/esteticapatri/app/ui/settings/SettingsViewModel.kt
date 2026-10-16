package com.esteticapatri.app.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esteticapatri.app.backup.BackupManager
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.data.local.WorkingHoursEntity
import com.esteticapatri.app.domain.WorkingHoursUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val workingHoursUseCases = WorkingHoursUseCases(db.workingHoursDao())
    val backupManager = BackupManager(application, db)

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _localBackups = MutableStateFlow<List<File>>(emptyList())
    val localBackups: StateFlow<List<File>> = _localBackups.asStateFlow()

    val workingHours: StateFlow<List<WorkingHoursEntity>> = workingHoursUseCases.observeWorkingHours().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            workingHoursUseCases.getOrInitializeDefaults()
            refreshLocalBackups()
        }
    }

    fun refreshLocalBackups() {
        _localBackups.value = backupManager.listLocalBackups()
    }

    fun updateDayHours(entity: WorkingHoursEntity) {
        viewModelScope.launch {
            workingHoursUseCases.updateWorkingHours(entity).fold(
                onSuccess = { _uiMessage.value = "Horario comercial actualizado." },
                onFailure = { _uiMessage.value = it.message ?: "Error al actualizar horario." }
            )
        }
    }

    fun createBackup(password: String?) {
        viewModelScope.launch {
            backupManager.createLocalBackup(password).fold(
                onSuccess = {
                    _uiMessage.value = "Copia de respaldo creada con éxito."
                    refreshLocalBackups()
                },
                onFailure = { _uiMessage.value = it.message ?: "Error al crear el respaldo." }
            )
        }
    }

    fun exportBackup(uri: Uri, password: String?) {
        viewModelScope.launch {
            backupManager.exportBackupWithPassword(uri.toString(), password).fold(
                onSuccess = { _uiMessage.value = "Respaldo exportado exitosamente." },
                onFailure = { _uiMessage.value = it.message ?: "Error al exportar respaldo." }
            )
        }
    }

    fun restoreBackup(uri: Uri, password: String?) {
        viewModelScope.launch {
            backupManager.restoreWithPassword(uri.toString(), password).fold(
                onSuccess = {
                    _uiMessage.value = "¡Datos restaurados con éxito! Se creó una copia preventiva."
                    refreshLocalBackups()
                },
                onFailure = { _uiMessage.value = it.message ?: "Error al restaurar respaldo." }
            )
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }
}
