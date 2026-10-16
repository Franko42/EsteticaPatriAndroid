package com.esteticapatri.app.ui.newappointment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.data.local.ServiceEntity
import com.esteticapatri.app.integrations.WhatsAppHelper
import com.esteticapatri.app.integrations.WhatsAppMessageBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentScreen(
    viewModel: NewAppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val services by viewModel.services.collectAsState()
    val selectedClientId by viewModel.selectedClientId.collectAsState()
    val selectedServiceId by viewModel.selectedServiceId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val appointmentCreated by viewModel.appointmentCreated.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var showClientDropdown by remember { mutableStateOf(false) }
    var showServiceDropdown by remember { mutableStateOf(false) }

    val selectedClient = clients.find { it.id == selectedClientId }
    val selectedService = services.find { it.id == selectedServiceId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendar Turno", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Client Selector
            Text("1. Seleccionar Clienta *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedTextField(
                    value = selectedClient?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Tocá para elegir una clienta") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClientDropdown = true }
                )
                DropdownMenu(
                    expanded = showClientDropdown,
                    onDismissRequest = { showClientDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (clients.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay clientas registradas. Agregá una primero.") },
                            onClick = { showClientDropdown = false }
                        )
                    } else {
                        clients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text("${client.name} ${client.phone?.let { "($it)" } ?: ""}") },
                                onClick = {
                                    viewModel.selectClient(client.id)
                                    showClientDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Service Selector
            Text("2. Tratamiento / Servicio *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedTextField(
                    value = selectedService?.let { "${it.name} (${it.durationMinutes} min)" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Tocá para elegir un tratamiento") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showServiceDropdown = true }
                )
                DropdownMenu(
                    expanded = showServiceDropdown,
                    onDismissRequest = { showServiceDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (services.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay tratamientos registrados. Agregá uno primero.") },
                            onClick = { showServiceDropdown = false }
                        )
                    } else {
                        services.forEach { service ->
                            DropdownMenuItem(
                                text = { Text("${service.name} (${service.durationMinutes} min)") },
                                onClick = {
                                    viewModel.selectService(service.id)
                                    showServiceDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Date & Time Selector
            Text("3. Fecha y Horario *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // Date Quick Row
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fecha: ${selectedDate.format(dateFormatter)}", fontWeight = FontWeight.Medium)
                        Row {
                            TextButton(onClick = { viewModel.selectDate(LocalDate.now()) }) {
                                Text("Hoy")
                            }
                            TextButton(onClick = { viewModel.selectDate(LocalDate.now().plusDays(1)) }) {
                                Text("Mañana")
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Time Row
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hora: ${selectedTime.format(timeFormatter)} hs", fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(9, 11, 14, 16, 18).forEach { hour ->
                                FilterChip(
                                    selected = selectedTime.hour == hour && selectedTime.minute == 0,
                                    onClick = { viewModel.selectTime(LocalTime.of(hour, 0)) },
                                    label = { Text("%02d:00".format(hour)) }
                                )
                            }
                        }
                    }
                }
            }

            // Duration & Total
            if (selectedService != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duración estimada: ${selectedService.durationMinutes} min")
                        selectedService.priceMinorUnits?.let { priceUnits ->
                            Text("Total: $ ${priceUnits / 100}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Notes
            Text("4. Observaciones (opcional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.onNotesChange(it) },
                placeholder = { Text("Ej: Trae diseño de uñas / piel sensible") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = { viewModel.submit() },
                enabled = selectedClientId != null && selectedServiceId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Confirmar y Agendar Turno", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Success Dialog with WhatsApp option
    appointmentCreated?.let { info ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearCreated()
                onNavigateBack()
            },
            title = { Text("¡Turno Agendado!", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Clienta: ${info.clientName}")
                    Text("Tratamiento: ${info.serviceName}")
                    Text("Fecha y hora: ${info.formattedDate} a las ${info.formattedTime} hs")
                    Spacer(Modifier.height(8.dp))
                    if (!info.clientPhone.isNullOrBlank()) {
                        Text(
                            "¿Querés abrir WhatsApp para enviar la confirmación a la clienta?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                if (!info.clientPhone.isNullOrBlank()) {
                    Button(
                        onClick = {
                            val msg = WhatsAppMessageBuilder.buildConfirmationMessage(
                                clientName = info.clientName,
                                serviceName = info.serviceName,
                                formattedDate = info.formattedDate,
                                formattedTime = info.formattedTime
                            )
                            WhatsAppHelper.launchChat(context, info.clientPhone, msg)
                            viewModel.clearCreated()
                            onNavigateBack()
                        }
                    ) {
                        Text("Abrir WhatsApp")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.clearCreated()
                            onNavigateBack()
                        }
                    ) {
                        Text("Listo")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCreated()
                        onNavigateBack()
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
