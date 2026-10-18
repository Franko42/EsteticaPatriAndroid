package com.esteticapatri.app.ui.newappointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.esteticapatri.app.R
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.data.local.ServiceEntity
import com.esteticapatri.app.integrations.WhatsAppHelper
import com.esteticapatri.app.integrations.WhatsAppMessageBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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

    var showClientDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val selectedClient = clients.find { it.id == selectedClientId }
    val selectedService = services.find { it.id == selectedServiceId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
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
                        Spacer(Modifier.width(8.dp))
                        Text("Agendar Turno", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
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
            Surface(
                onClick = { showClientDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (selectedClient != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                color = if (selectedClient != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (selectedClient != null) {
                            Text(
                                text = selectedClient.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            selectedClient.phone?.let { phone ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Tocá para elegir una clienta",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (clients.isEmpty()) "No hay clientas cargadas" else "${clients.size} clientas disponibles",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Service Selector
            Text("2. Tratamiento / Servicio *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Surface(
                onClick = { showServiceDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (selectedService != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                color = if (selectedService != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (selectedService != null) {
                            Text(
                                text = selectedService.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val priceText = selectedService.priceMinorUnits?.let { " • $${it / 100}" } ?: ""
                            Text(
                                text = "${selectedService.durationMinutes} minutos$priceText",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Tocá para elegir un tratamiento",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (services.isEmpty()) "No hay tratamientos cargados" else "${services.size} tratamientos disponibles",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Date & Time Selector
            Text("3. Fecha y Horario *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", Locale("es", "ES"))
            val formattedDateText = selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }

            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Date row with custom picker button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fecha de turno", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formattedDateText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                        }
                        OutlinedButton(
                            onClick = { showDatePickerDialog = true },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cambiar")
                        }
                    }

                    // Quick Date Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedDate == LocalDate.now(),
                            onClick = { viewModel.selectDate(LocalDate.now()) },
                            label = { Text("Hoy") },
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        )
                        FilterChip(
                            selected = selectedDate == LocalDate.now().plusDays(1),
                            onClick = { viewModel.selectDate(LocalDate.now().plusDays(1)) },
                            label = { Text("Mañana") },
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        )
                        FilterChip(
                            selected = selectedDate == LocalDate.now().plusDays(7),
                            onClick = { viewModel.selectDate(LocalDate.now().plusDays(7)) },
                            label = { Text("+7 días") },
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        )
                    }

                    HorizontalDivider()

                    // Time Row with full picker
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hora de inicio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selectedTime.format(timeFormatter)} hs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(
                            onClick = { showTimePickerDialog = true },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Elegir hora")
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
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Duración estimada: ${selectedService.durationMinutes} min", style = MaterialTheme.typography.bodyMedium)
                        selectedService.priceMinorUnits?.let { priceUnits ->
                            Text("Total: $ ${priceUnits / 100}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Notes
            Text("4. Observaciones (opcional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.onNotesChange(it) },
                placeholder = { Text("Ej: Trae diseño propio / alergia a productos") },
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

    // Client Selection Dialog
    if (showClientDialog) {
        var clientFilter by remember { mutableStateOf("") }
        val filteredClients = remember(clients, clientFilter) {
            if (clientFilter.isBlank()) clients
            else clients.filter {
                it.name.contains(clientFilter, ignoreCase = true) ||
                (it.phone != null && it.phone.contains(clientFilter))
            }
        }

        AlertDialog(
            onDismissRequest = { showClientDialog = false },
            title = {
                Text(
                    text = "Seleccionar Clienta",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    if (clients.isNotEmpty()) {
                        OutlinedTextField(
                            value = clientFilter,
                            onValueChange = { clientFilter = it },
                            placeholder = { Text("Buscar clienta...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    if (clients.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Aún no hay clientas registradas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Agregá clientas en la pestaña 'Clientas'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (filteredClients.isEmpty()) {
                        Text(
                            text = "No se encontraron clientas para '$clientFilter'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(filteredClients) { client ->
                                Surface(
                                    onClick = {
                                        viewModel.selectClient(client.id)
                                        showClientDialog = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (client.id == selectedClientId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = client.name,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            client.phone?.let { phone ->
                                                Text(
                                                    text = phone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (client.id == selectedClientId) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionada",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClientDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Service Selection Dialog
    if (showServiceDialog) {
        var serviceFilter by remember { mutableStateOf("") }
        val filteredServices = remember(services, serviceFilter) {
            if (serviceFilter.isBlank()) services
            else services.filter {
                it.name.contains(serviceFilter, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showServiceDialog = false },
            title = {
                Text(
                    text = "Seleccionar Tratamiento",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    if (services.isNotEmpty()) {
                        OutlinedTextField(
                            value = serviceFilter,
                            onValueChange = { serviceFilter = it },
                            placeholder = { Text("Buscar tratamiento...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    if (services.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Aún no hay tratamientos registrados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Agregá tratamientos en la pestaña 'Servicios'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (filteredServices.isEmpty()) {
                        Text(
                            text = "No se encontraron tratamientos para '$serviceFilter'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(filteredServices) { service ->
                                Surface(
                                    onClick = {
                                        viewModel.selectService(service.id)
                                        showServiceDialog = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (service.id == selectedServiceId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = service.name,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val priceText = service.priceMinorUnits?.let { " • $${it / 100}" } ?: ""
                                            Text(
                                                text = "${service.durationMinutes} min$priceText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (service.id == selectedServiceId) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showServiceDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val local = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.selectDate(local)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Selection Dialog (supporting 08:00 to 20:45 in 15-minute increments)
    if (showTimePickerDialog) {
        var hourSelection by remember { mutableStateOf(selectedTime.hour) }
        var minuteSelection by remember { mutableStateOf(selectedTime.minute) }

        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text("Seleccionar Horario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Hora: %02d:00".format(hourSelection), fontWeight = FontWeight.SemiBold)
                    // Common hours row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(8, 9, 10, 11, 14, 16, 18, 20).forEach { h ->
                            FilterChip(
                                selected = hourSelection == h,
                                onClick = { hourSelection = h },
                                label = { Text("%02d".format(h)) }
                            )
                        }
                    }

                    Text("Minutos: %02d".format(minuteSelection), fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0, 15, 30, 45).forEach { m ->
                            FilterChip(
                                selected = minuteSelection == m,
                                onClick = { minuteSelection = m },
                                label = { Text(":%02d".format(m)) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.selectTime(LocalTime.of(hourSelection, minuteSelection))
                        showTimePickerDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
