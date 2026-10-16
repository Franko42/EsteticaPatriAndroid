package com.esteticapatri.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteticapatri.app.data.local.WorkingHoursEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val workingHours by viewModel.workingHours.collectAsState()
    val localBackups by viewModel.localBackups.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var passwordForBackup by remember { mutableStateOf<String?>(null) }
    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it, passwordForBackup) }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            showRestorePasswordDialog = true
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
                title = { Text("Configuración y Respaldo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline & Privacy Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "100% Offline y Privado",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Todos los datos de clientas y turnos permanecen únicamente en este dispositivo. Sin servidores externos ni permisos de internet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Backups Section
            item {
                Text(
                    "Copias de Respaldo (Backup)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedCard(shape = RoundedCornerShape(12.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Exportá o restaurá tus datos usando Storage Access Framework de manera segura. Cada restauración genera automáticamente un respaldo preventivo de tus datos vigentes.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCreateBackupDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Crear local")
                            }

                            OutlinedButton(
                                onClick = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    exportFileLauncher.launch("estetica_patri_backup_$dateStr.json")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Exportar")
                            }
                        }

                        OutlinedButton(
                            onClick = { importFileLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restaurar desde archivo")
                        }
                    }
                }
            }

            // Local Backups List
            if (localBackups.isNotEmpty()) {
                item {
                    Text(
                        "Respaldos locales en dispositivo (${localBackups.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(localBackups) { file ->
                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(file.lastModified()))
                    val sizeKb = file.length() / 1024
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$dateStr • $sizeKb KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            // Working Hours Section
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Horarios de Atención",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(workingHours) { entity ->
                WorkingHoursRow(
                    entity = entity,
                    onToggle = { enabled ->
                        viewModel.updateDayHours(entity.copy(enabled = enabled))
                    }
                )
            }
        }
    }

    if (showCreateBackupDialog) {
        var pwd by remember { mutableStateOf("") }
        var usePassword by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateBackupDialog = false },
            title = { Text("Crear copia de respaldo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se guardará una copia instantánea y consistente de todas las clientas, tratamientos y turnos.")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Checkbox(checked = usePassword, onCheckedChange = { usePassword = it })
                        Spacer(Modifier.width(6.dp))
                        Text("Cifrar con contraseña (AES-GCM)")
                    }
                    if (usePassword) {
                        OutlinedTextField(
                            value = pwd,
                            onValueChange = { pwd = it },
                            label = { Text("Contraseña de cifrado") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pass = if (usePassword) pwd.takeIf { it.isNotBlank() } else null
                        viewModel.createBackup(pass)
                        showCreateBackupDialog = false
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Crear ahora")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateBackupDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRestorePasswordDialog && pendingRestoreUri != null) {
        var pwd by remember { mutableStateOf("") }
        var isEncrypted by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showRestorePasswordDialog = false
                pendingRestoreUri = null
            },
            title = { Text("Confirmar Restauración", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se verificará el checksum SHA-256 y la integridad antes de reemplazar datos. Además, se creará automáticamente un respaldo preventivo de tus datos actuales.")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Checkbox(checked = isEncrypted, onCheckedChange = { isEncrypted = it })
                        Spacer(Modifier.width(6.dp))
                        Text("El archivo está cifrado con contraseña")
                    }
                    if (isEncrypted) {
                        OutlinedTextField(
                            value = pwd,
                            onValueChange = { pwd = it },
                            label = { Text("Contraseña del archivo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pass = if (isEncrypted) pwd.takeIf { it.isNotBlank() } else null
                        viewModel.restoreBackup(pendingRestoreUri!!, pass)
                        showRestorePasswordDialog = false
                        pendingRestoreUri = null
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestorePasswordDialog = false
                        pendingRestoreUri = null
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun WorkingHoursRow(
    entity: WorkingHoursEntity,
    onToggle: (Boolean) -> Unit
) {
    val dayName = when (entity.dayOfWeek) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> "Día ${entity.dayOfWeek}"
    }

    val startH = entity.startMinuteOfDay / 60
    val startM = entity.startMinuteOfDay % 60
    val endH = entity.endMinuteOfDay / 60
    val endM = entity.endMinuteOfDay % 60

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entity.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dayName, fontWeight = FontWeight.SemiBold)
                if (entity.enabled) {
                    Text(
                        "%02d:%02d a %02d:%02d hs".format(startH, startM, endH, endM),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Cerrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Switch(
                checked = entity.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.semantics {
                    contentDescription = "Activar o desactivar horario para $dayName"
                }
            )
        }
    }
}
