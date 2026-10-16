package com.esteticapatri.app.ui.agenda

import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.AppointmentWithDetails
import com.esteticapatri.app.integrations.WhatsAppHelper
import com.esteticapatri.app.integrations.WhatsAppMessageBuilder
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
                title = { Text("Agenda de Turnos", fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.size(56.dp)
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

            HorizontalDivider()

            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay turnos para este día",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToNewAppointment,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Agendar un nuevo turno")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusColors.noShow()) },
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
            Icon(Icons.Default.ArrowBack, contentDescription = "Día anterior")
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(
            onClick = onNextDay,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Día siguiente")
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

    val (statusLabel, statusColor) = when (item.appointment.status) {
        AppointmentEntity.SCHEDULED -> "Programado" to StatusColors.scheduled()
        AppointmentEntity.COMPLETED -> "Completado" to StatusColors.completed()
        AppointmentEntity.CANCELLED -> "Cancelado" to StatusColors.cancelled()
        AppointmentEntity.NO_SHOW -> "No asistió" to StatusColors.noShow()
        else -> item.appointment.status to MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Client Name & Phone
            Text(
                text = item.client.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            item.client.phone?.let { phone ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Tel: $phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Notes
            item.appointment.notes?.let { notes ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nota: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons with >= 48dp touch targets and 8dp spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Button
                item.client.phone?.let { phone ->
                    IconButton(
                        onClick = {
                            val msg = WhatsAppMessageBuilder.buildReminderMessage(
                                clientName = item.client.name,
                                serviceName = item.service.name,
                                formattedDate = dateStr,
                                formattedTime = startTime
                            )
                            WhatsAppHelper.launchChat(context, phone, msg)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar recordatorio por WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                if (item.appointment.status == AppointmentEntity.SCHEDULED) {
                    IconButton(
                        onClick = onComplete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Marcar completado",
                            tint = StatusColors.completed()
                        )
                    }
                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = onRequestNoShow,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Marcar no asistió",
                            tint = StatusColors.noShow()
                        )
                    }
                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = onRequestCancel,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancelar turno",
                            tint = StatusColors.cancelled()
                        )
                    }
                }
            }
        }
    }
}
