package com.esteticapatri.app.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esteticapatri.app.backup.BackupManager
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.data.local.WorkingHoursEntity
import com.esteticapatri.app.domain.WorkingHoursUseCases
import com.esteticapatri.app.updater.AppUpdateInfo
import com.esteticapatri.app.updater.AppUpdateManager
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
    val updateManager = AppUpdateManager(application)

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _localBackups = MutableStateFlow<List<File>>(emptyList())
    val localBackups: StateFlow<List<File>> = _localBackups.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

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

    fun checkForUpdates(userInitiated: Boolean = true) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            updateManager.checkForUpdates().fold(
                onSuccess = { info ->
                    _isCheckingUpdate.value = false
                    _updateInfo.value = info
                    if (!info.isUpdateAvailable && userInitiated) {
                        _uiMessage.value = "Tenés instalada la versión más reciente (${info.currentVersion})."
                    }
                },
                onFailure = { error ->
                    _isCheckingUpdate.value = false
                    if (userInitiated) {
                        _uiMessage.value = "No se pudo verificar actualizaciones: ${error.message ?: "Sin conexión"}"
                    }
                }
            )
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
        _downloadProgress.value = null
    }

    fun downloadAndInstallUpdate(info: AppUpdateInfo) {
        val url = info.apkDownloadUrl ?: return
        val fileName = info.apkFileName ?: "EsteticaPatri-${info.latestVersion}.apk"

        viewModelScope.launch {
            _downloadProgress.value = 0.01f
            updateManager.downloadApk(url, fileName) { progress ->
                _downloadProgress.value = progress
            }.fold(
                onSuccess = { apkFile ->
                    _downloadProgress.value = null
                    _updateInfo.value = null
                    updateManager.startInstall(apkFile)
                },
                onFailure = { error ->
                    _downloadProgress.value = null
                    _uiMessage.value = "Error al descargar actualización: ${error.message}"
                }
            )
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
