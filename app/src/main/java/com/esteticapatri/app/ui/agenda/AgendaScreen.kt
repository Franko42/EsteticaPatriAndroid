package com.esteticapatri.app.ui.agenda

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteticapatri.app.R
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.AppointmentWithDetails
import com.esteticapatri.app.integrations.WhatsAppHelper
import com.esteticapatri.app.integrations.WhatsAppMessageBuilder
import com.esteticapatri.app.ui.components.EmptyStateCard
import com.esteticapatri.app.ui.theme.PastelOutline
import com.esteticapatri.app.ui.theme.RoseBlushGradient
import com.esteticapatri.app.ui.theme.StatusColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel,
    onNavigateToNewAppointment: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    var appointmentToCancel by remember { mutableStateOf<AppointmentWithDetails?>(null) }
    var appointmentToNoShow by remember { mutableStateOf<AppointmentWithDetails?>(null) }

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
                        Text("Estética Patri Turnos", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(
                        onClick = { viewModel.onToday() },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Hoy", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewAppointment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo turno")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date Selector Header
            DateNavigationHeader(
                selectedDate = selectedDate,
                onPreviousDay = { viewModel.onPreviousDay() },
                onNextDay = { viewModel.onNextDay() }
            )

            // Daily Summary Chips (when appointments exist)
            if (appointments.isNotEmpty()) {
                val scheduledCount = appointments.count { it.appointment.status == AppointmentEntity.SCHEDULED }
                val completedCount = appointments.count { it.appointment.status == AppointmentEntity.COMPLETED }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("$scheduledCount pendientes", fontWeight = FontWeight.Medium) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                    if (completedCount > 0) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("$completedCount atendidos", fontWeight = FontWeight.Medium) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = PastelOutline.copy(alpha = 0.5f))

            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        imageResId = R.drawable.illustration_empty_calendar,
                        title = "Día sin turnos agendados",
                        description = "No hay turnos registrados para esta fecha. Podés programar citas de manicura, faciales o pestañas tocando el botón.",
                        actionLabel = "Agendar Turno",
                        actionIcon = Icons.Default.Add,
                        onActionClick = onNavigateToNewAppointment
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(appointments, key = { it.appointment.id }) { item ->
                        AppointmentItemCard(
                            item = item,
                            context = context,
                            onComplete = { viewModel.complete(item.appointment.id) },
                            onRequestCancel = { appointmentToCancel = item },
                            onRequestNoShow = { appointmentToNoShow = item }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for Cancel
    appointmentToCancel?.let { item ->
        AlertDialog(
            onDismissRequest = { appointmentToCancel = null },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Cancelar este turno?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Se cancelará el turno de ${item.client.name} para ${item.service.name}. El registro permanecerá en el historial como Cancelado y liberará el horario.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancel(item.appointment.id)
                        appointmentToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar cancelación")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToCancel = null }) {
                    Text("Volver")
                }
            }
        )
    }

    // Confirmation dialog for No-Show
    appointmentToNoShow?.let { item ->
        AlertDialog(
            onDismissRequest = { appointmentToNoShow = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusColors.noShow().color) },
            title = { Text("¿Marcar que no asistió?", fontWeight = FontWeight.Bold) },
            text = {
                Text("El turno de ${item.client.name} se marcará como No Asistió.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markNoShow(item.appointment.id)
                        appointmentToNoShow = null
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToNoShow = null }) {
                    Text("Volver")
                }
            }
        )
    }
}

@Composable
private fun DateNavigationHeader(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val dateText = selectedDate.format(formatter).replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousDay,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Día anterior", tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(
            onClick = onNextDay,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Día siguiente", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AppointmentItemCard(
    item: AppointmentWithDetails,
    context: Context,
    onComplete: () -> Unit,
    onRequestCancel: () -> Unit,
    onRequestNoShow: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    val startTime = Instant.ofEpochMilli(item.appointment.startEpochMillis).atZone(zone).format(timeFormatter)
    val endTime = Instant.ofEpochMilli(item.appointment.endEpochMillis).atZone(zone).format(timeFormatter)
    val dateStr = Instant.ofEpochMilli(item.appointment.startEpochMillis).atZone(zone).format(dateFormatter)
    var menuExpanded by remember { mutableStateOf(false) }

    val (statusLabel, token) = when (item.appointment.status) {
        AppointmentEntity.SCHEDULED -> "Programado" to StatusColors.scheduled()
        AppointmentEntity.COMPLETED -> "Completado" to StatusColors.completed()
        AppointmentEntity.CANCELLED -> "Cancelado" to StatusColors.cancelled()
        AppointmentEntity.NO_SHOW -> "No asistió" to StatusColors.noShow()
        else -> item.appointment.status to StatusColors.scheduled()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelOutline.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .background(RoseBlushGradient)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Range
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$startTime - $endTime hs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status Badge & Dropdown Action Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = token.container
                    ) {
                        Text(
                            text = statusLabel,
                            color = token.color,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones del turno",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            item.client.phone?.let { phone ->
                                DropdownMenuItem(
                                    text = { Text("Enviar WhatsApp") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = null,
                                            tint = Color(0xFF25D366)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        val msg = WhatsAppMessageBuilder.buildReminderMessage(
                                            clientName = item.client.name,
                                            serviceName = item.service.name,
                                            formattedDate = dateStr,
                                            formattedTime = startTime
                                        )
                                        WhatsAppHelper.launchChat(context, phone, msg)
                                    }
                                )
                            }

                            if (item.appointment.status == AppointmentEntity.SCHEDULED) {
                                DropdownMenuItem(
                                    text = { Text("Marcar atendido") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusColors.completed().color
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onComplete()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Marcar que no asistió") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = StatusColors.noShow().color
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onRequestNoShow()
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Cancelar turno",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onRequestCancel()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Client Name & Phone
            Text(
                text = item.client.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            item.client.phone?.let { phone ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Tel: $phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            // Service Name, Duration & Price with real vector icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Spa,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${item.service.name} (${item.service.durationMinutes} min)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                item.service.priceMinorUnits?.let { priceUnits ->
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$ ${priceUnits / 100}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Notes
            item.appointment.notes?.let { notes ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nota: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
