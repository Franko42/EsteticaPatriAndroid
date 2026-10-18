package com.esteticapatri.app.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.data.local.DatabaseProvider
import com.esteticapatri.app.domain.CreateClient
import com.esteticapatri.app.domain.DeactivateClient
import com.esteticapatri.app.domain.GetClients
import com.esteticapatri.app.domain.UpdateClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.content.Context
import com.esteticapatri.app.contacts.PhoneContact
import com.esteticapatri.app.contacts.PhoneContactsManager
import com.esteticapatri.app.domain.ClientImportCandidate
import com.esteticapatri.app.domain.ImportClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClientsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val getClients = GetClients(db.clientDao())
    private val createClient = CreateClient(db.clientDao())
    private val updateClient = UpdateClient(db.clientDao())
    private val deactivateClient = DeactivateClient(db.clientDao())
    private val importClients = ImportClients(db.clientDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Estados para la importación en lote de contactos
    private val _showBatchImportDialog = MutableStateFlow(false)
    val showBatchImportDialog: StateFlow<Boolean> = _showBatchImportDialog.asStateFlow()

    private val _isLoadingContacts = MutableStateFlow(false)
    val isLoadingContacts: StateFlow<Boolean> = _isLoadingContacts.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _phoneContacts = MutableStateFlow<List<PhoneContact>>(emptyList())
    val phoneContacts: StateFlow<List<PhoneContact>> = _phoneContacts.asStateFlow()

    private val _selectedContactKeys = MutableStateFlow<Set<String>>(emptySet())
    val selectedContactKeys: StateFlow<Set<String>> = _selectedContactKeys.asStateFlow()

    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val clients: StateFlow<List<ClientEntity>> = _searchQuery.flatMapLatest { query ->
        getClients.search(query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addClient(name: String, phone: String?, notes: String?) {
        viewModelScope.launch {
            createClient(name, phone, notes).fold(
                onSuccess = { _uiMessage.value = "Clienta guardada con éxito." },
                onFailure = { _uiMessage.value = it.message ?: "Error al guardar clienta." }
            )
        }
    }

    fun editClient(client: ClientEntity) {
        viewModelScope.launch {
            updateClient(client).fold(
                onSuccess = { _uiMessage.value = "Clienta actualizada con éxito." },
                onFailure = { _uiMessage.value = it.message ?: "Error al actualizar clienta." }
            )
        }
    }

    fun removeClient(id: Long) {
        viewModelScope.launch {
            deactivateClient(id).fold(
                onSuccess = { _uiMessage.value = "Clienta dada de baja." },
                onFailure = { _uiMessage.value = it.message ?: "Error al dar de baja." }
            )
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun openBatchImportDialog(context: Context) {
        viewModelScope.launch {
            _isLoadingContacts.value = true
            _contactSearchQuery.value = ""
            _selectedContactKeys.value = emptySet()

            val existingClients = withContext(Dispatchers.IO) {
                db.clientDao().getAllActive()
            }
            val existingNormalizedPhones = existingClients.mapNotNull { it.normalizedPhone }.toSet()
            val existingNames = existingClients.map { it.name.trim().lowercase() }.toSet()

            val fetched = withContext(Dispatchers.IO) {
                PhoneContactsManager.fetchPhoneContacts(
                    context = context,
                    existingNormalizedPhones = existingNormalizedPhones,
                    existingNames = existingNames
                )
            }

            _phoneContacts.value = fetched
            _isLoadingContacts.value = false
            _showBatchImportDialog.value = true
        }
    }

    fun closeBatchImportDialog() {
        _showBatchImportDialog.value = false
        _phoneContacts.value = emptyList()
        _selectedContactKeys.value = emptySet()
        _contactSearchQuery.value = ""
    }

    fun onContactSearchQueryChange(query: String) {
        _contactSearchQuery.value = query
    }

    fun toggleContactSelection(key: String) {
        val current = _selectedContactKeys.value.toMutableSet()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }
        _selectedContactKeys.value = current
    }

    fun toggleSelectAll(select: Boolean, filteredContacts: List<PhoneContact>) {
        if (select) {
            val selectableKeys = filteredContacts
                .filter { !it.isAlreadyRegistered }
                .map { it.contactKey() }
                .toSet()
            _selectedContactKeys.value = _selectedContactKeys.value + selectableKeys
        } else {
            val keysToRemove = filteredContacts.map { it.contactKey() }.toSet()
            _selectedContactKeys.value = _selectedContactKeys.value - keysToRemove
        }
    }

    fun importSelectedContacts() {
        val keys = _selectedContactKeys.value
        if (keys.isEmpty()) {
            _uiMessage.value = "No seleccionaste ninguna clienta para importar."
            return
        }

        viewModelScope.launch {
            _isImporting.value = true
            val candidates = _phoneContacts.value
                .filter { keys.contains(it.contactKey()) }
                .map {
                    ClientImportCandidate(
                        name = it.name,
                        phone = it.phoneNumber,
                        notes = "Importada desde contactos del teléfono"
                    )
                }

            val result = withContext(Dispatchers.IO) {
                importClients(candidates)
            }

            _isImporting.value = false
            result.fold(
                onSuccess = { count ->
                    _uiMessage.value = if (count > 0) {
                        "Se importaron $count clientas con éxito."
                    } else {
                        "No se importaron clientas nuevas (ya se encontraban registradas)."
                    }
                    closeBatchImportDialog()
                },
                onFailure = {
                    _uiMessage.value = it.message ?: "Error al importar clientas."
                }
            )
        }
    }
}

fun PhoneContact.contactKey(): String =
    if (normalizedPhone.isNotEmpty()) normalizedPhone else "${name.lowercase()}_$id"

