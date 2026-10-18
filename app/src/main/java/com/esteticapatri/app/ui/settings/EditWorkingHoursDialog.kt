package com.esteticapatri.app.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteticapatri.app.data.local.WorkingHoursEntity

@Composable
fun EditWorkingHoursDialog(
    entity: WorkingHoursEntity,
    onDismiss: () -> Unit,
    onSave: (WorkingHoursEntity) -> Unit
) {
    val context = LocalContext.current
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

    var startMinute by remember { mutableIntStateOf(entity.startMinuteOfDay) }
    var endMinute by remember { mutableIntStateOf(entity.endMinuteOfDay) }

    val startH = startMinute / 60
    val startM = startMinute % 60
    val endH = endMinute / 60
    val endM = endMinute % 60

    val isValid = endMinute > startMinute

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Configurar horario: $dayName",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Tocá cada tarjeta para definir la hora de apertura y de cierre con el reloj del sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Apertura
                    OutlinedCard(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> startMinute = h * 60 + m },
                                startH,
                                startM,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Apertura",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "%02d:%02d".format(startH, startM),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Cierre
                    OutlinedCard(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> endMinute = h * 60 + m },
                                endH,
                                endM,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Cierre",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "%02d:%02d".format(endH, endM),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Presets rápidos
                Text(
                    "Horarios predeterminados:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            startMinute = 9 * 60
                            endMinute = 19 * 60
                        },
                        label = { Text("09 a 19 hs", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = {
                            startMinute = 8 * 60
                            endMinute = 13 * 60
                        },
                        label = { Text("08 a 13 hs", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = {
                            startMinute = 14 * 60
                            endMinute = 20 * 60
                        },
                        label = { Text("14 a 20 hs", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                if (!isValid) {
                    Text(
                        "El horario de cierre debe ser posterior a la apertura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        entity.copy(
                            startMinuteOfDay = startMinute,
                            endMinuteOfDay = endMinute
                        )
                    )
                },
                enabled = isValid
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
