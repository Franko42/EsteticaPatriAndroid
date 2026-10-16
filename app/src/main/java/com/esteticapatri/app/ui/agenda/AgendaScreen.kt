package com.esteticapatri.app.ui.agenda

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.AppointmentWithDetails
import com.esteticapatri.app.integrations.WhatsAppHelper
import com.esteticapatri.app.integrations.WhatsAppMessageBuilder
import com.esteticapatri.app.ui.theme.StatusCancelled
import com.esteticapatri.app.ui.theme.StatusCompleted
import com.esteticapatri.app.ui.theme.StatusNoShow
import com.esteticapatri.app.ui.theme.StatusScheduled
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
                    TextButton(onClick = { viewModel.onToday() }) {
                        Text("Hoy", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewAppointment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onNavigateToNewAppointment) {
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
                            onCancel = { viewModel.cancel(item.appointment.id) },
                            onNoShow = { viewModel.markNoShow(item.appointment.id) }
                        )
                    }
                }
            }
        }
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
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Día anterior")
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextDay) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Día siguiente")
        }
    }
}

@Composable
private fun AppointmentItemCard(
    item: AppointmentWithDetails,
    context: Context,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onNoShow: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    val startTime = Instant.ofEpochMilli(item.appointment.startEpochMillis).atZone(zone).format(timeFormatter)
    val endTime = Instant.ofEpochMilli(item.appointment.endEpochMillis).atZone(zone).format(timeFormatter)
    val dateStr = Instant.ofEpochMilli(item.appointment.startEpochMillis).atZone(zone).format(dateFormatter)

    val (statusLabel, statusColor) = when (item.appointment.status) {
        AppointmentEntity.SCHEDULED -> "Programado" to StatusScheduled
        AppointmentEntity.COMPLETED -> "Completado" to StatusCompleted
        AppointmentEntity.CANCELLED -> "Cancelado" to StatusCancelled
        AppointmentEntity.NO_SHOW -> "No asistió" to StatusNoShow
        else -> item.appointment.status to Color.Gray
    }

    var showActionMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "$startTime - $endTime",
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
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Client Name & Phone
            Text(
                text = item.client.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            item.client.phone?.let { phone ->
                Text(
                    text = "Tel: $phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            // Service Name & Price
            val priceStr = item.service.priceMinorUnits?.let { "$ ${it / 100}" } ?: ""
            Text(
                text = "${item.service.name} (${item.service.durationMinutes} min) $priceStr",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            // Notes
            item.appointment.notes?.let { notes ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Nota: $notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Button (only if client has phone)
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
                        }
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar recordatorio por WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                }

                if (item.appointment.status == AppointmentEntity.SCHEDULED) {
                    IconButton(onClick = onComplete) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Marcar completado",
                            tint = StatusCompleted
                        )
                    }

                    IconButton(onClick = onNoShow) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Marcar no asistió",
                            tint = StatusNoShow
                        )
                    }

                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancelar turno",
                            tint = StatusCancelled
                        )
                    }
                }
            }
        }
    }
}
