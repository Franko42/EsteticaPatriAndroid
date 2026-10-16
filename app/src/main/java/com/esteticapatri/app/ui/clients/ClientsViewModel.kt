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

class ClientsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val getClients = GetClients(db.clientDao())
    private val createClient = CreateClient(db.clientDao())
    private val updateClient = UpdateClient(db.clientDao())
    private val deactivateClient = DeactivateClient(db.clientDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

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
}
