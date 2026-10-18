package com.esteticapatri.app.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.esteticapatri.app.R
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.integrations.WhatsAppHelper
import com.esteticapatri.app.ui.components.EmptyStateCard
import com.esteticapatri.app.ui.theme.LavenderBlushGradient
import com.esteticapatri.app.ui.theme.PastelOutline

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.esteticapatri.app.contacts.PhoneContactsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel
) {
    val clients by viewModel.clients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val showBatchImportDialog by viewModel.showBatchImportDialog.collectAsState()
    val phoneContacts by viewModel.phoneContacts.collectAsState()
    val selectedContactKeys by viewModel.selectedContactKeys.collectAsState()
    val contactSearchQuery by viewModel.contactSearchQuery.collectAsState()
    val isLoadingContacts by viewModel.isLoadingContacts.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openBatchImportDialog(context)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Se requiere permiso de Contactos para la importación en lote.")
            }
        }
    }

    val requestContactsImport = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.openBatchImportDialog(context)
        } else {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            shadowElevation = 2.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_estetica),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Directorio de Clientas", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(
                        onClick = requestContactsImport,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "Importar clientas desde contactos",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva clienta")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Buscar clienta por nombre o teléfono...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChange("") },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            if (clients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyStateCard(
                            imageResId = R.drawable.illustration_clients_beauty,
                            title = if (searchQuery.isEmpty()) "Aún no hay clientas registradas" else "No se encontraron clientas",
                            description = if (searchQuery.isEmpty()) "Comenzá a registrar a tus clientas para agendar turnos y contactarlas directamente por WhatsApp." else "Probá buscando con otro nombre o número.",
                            actionLabel = if (searchQuery.isEmpty()) "Agregar Clienta" else null,
                            actionIcon = Icons.Default.PersonAdd,
                            onActionClick = { showAddDialog = true }
                        )

                        if (searchQuery.isEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = requestContactsImport,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Importar desde contactos del teléfono")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(clients, key = { it.id }) { client ->
                        ClientItemCard(
                            client = client,
                            onWhatsAppClick = { phone ->
                                WhatsAppHelper.launchChat(
                                    context,
                                    phone,
                                    "¡Hola ${client.name}! Nos comunicamos desde Estética Patri."
                                )
                            },
                            onEditClick = { clientToEdit = client },
                            onDeleteClick = { clientToDelete = client }
                        )
                    }
                }
            }
        }
    }

    if (showBatchImportDialog) {
        BatchImportContactsDialog(
            contacts = phoneContacts,
            selectedKeys = selectedContactKeys,
            searchQuery = contactSearchQuery,
            isLoading = isLoadingContacts,
            isImporting = isImporting,
            onSearchQueryChange = { viewModel.onContactSearchQueryChange(it) },
            onToggleSelect = { viewModel.toggleContactSelection(it) },
            onToggleSelectAll = { select, filtered -> viewModel.toggleSelectAll(select, filtered) },
            onConfirmImport = { viewModel.importSelectedContacts() },
            onDismiss = { viewModel.closeBatchImportDialog() }
        )
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            icon = {
                Icon(
                    Icons.Default.Contacts,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Permiso de Contactos", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Estética Patri necesita acceso de lectura a tus contactos para permitirte seleccionar e importar clientas a tu agenda local.\n\nLa aplicación es 100% offline y tus contactos jamás saldrán de tu dispositivo."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddDialog) {
        ClientFormDialog(
            title = "Nueva Clienta",
            initialName = "",
            initialPhone = "",
            initialNotes = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, notes ->
                viewModel.addClient(name, phone, notes)
                showAddDialog = false
            }
        )
    }

    clientToEdit?.let { client ->
        ClientFormDialog(
            title = "Editar Clienta",
            initialName = client.name,
            initialPhone = client.phone ?: "",
            initialNotes = client.notes ?: "",
            onDismiss = { clientToEdit = null },
            onConfirm = { name, phone, notes ->
                viewModel.editClient(client.copy(name = name, phone = phone, notes = notes))
                clientToEdit = null
            }
        )
    }

    // Confirmation dialog before deleting/deactivating a client
    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Dar de baja a esta clienta?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Se desactivará a ${client.name}. Sus turnos históricos se mantendrán protegidos pero ya no aparecerá en la lista activa.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeClient(client.id)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Dar de baja")
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ClientItemCard(
    client: ClientEntity,
    onWhatsAppClick: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .background(LavenderBlushGradient)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Avatar
            val initials = client.name.split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")

            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                client.phone?.let { phone ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                client.notes?.let { notes ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                client.phone?.let { phone ->
                    IconButton(
                        onClick = { onWhatsAppClick(phone) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Contactar por WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.width(2.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Dar de baja", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ClientFormDialog(
    title: String,
    initialName: String,
    initialPhone: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String?, notes: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var notes by remember { mutableStateOf(initialNotes) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            PhoneContactsManager.getContactFromUri(context, it)?.let { contact ->
                if (contact.name.isNotBlank()) name = contact.name
                if (contact.phoneNumber.isNotBlank()) phone = contact.phoneNumber
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { contactPickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Elegir de contactos del teléfono")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre y Apellido *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono (con código de país)") },
                    placeholder = { Text("Ej: 5493764000000") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Preferencias estéticas") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone.takeIf { it.isNotBlank() }, notes.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

