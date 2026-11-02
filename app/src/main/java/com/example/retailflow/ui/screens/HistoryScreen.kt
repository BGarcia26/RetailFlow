package com.example.retailflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.theme.BrandBlue
import com.example.retailflow.ui.theme.InfoBg
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.ui.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onShiftClick: (Int) -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()
    val currentEntries by viewModel.entriesList.collectAsState()
    val currentShiftPeople = currentEntries.sumOf { it.groupSize }

    // Estados para filtrar por Fecha y Hora/Turno
    var dateQuery by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf("Todos") }
    val timeSlots = listOf("Todos", "Mañana", "Tarde", "Noche")
    var showCloseConfirm by remember { mutableStateOf(false) }

    // remember(...) recalcula el filtro SOLO si cambia la lista o alguno de los filtros
    val filteredList = remember(historyList, dateQuery, selectedTimeSlot) {
        historyList.filter { record ->
            val matchesDate = record.dateLabel.contains(dateQuery, ignoreCase = true) ||
                record.shiftTime.contains(dateQuery, ignoreCase = true)
            val matchesSlot = selectedTimeSlot == "Todos" ||
                record.shiftTime.startsWith(selectedTimeSlot, ignoreCase = true)
            matchesDate && matchesSlot
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tarjeta para archivar el turno en curso: aparece/desaparece con animación
        AnimatedVisibility(visible = currentShiftPeople > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = InfoBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Turno Actual en Curso: $currentShiftPeople personas registradas",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showCloseConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Cerrar Turno y Guardar en Historial")
                    }
                }
            }
        }

        // 1. Filtro por Fecha o Hora exacta
        OutlinedTextField(
            value = dateQuery,
            onValueChange = { dateQuery = it },
            label = { Text("Filtrar por fecha, día u hora (ej: 14/08, Hoy)") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Fecha") },
            trailingIcon = {
                if (dateQuery.isNotEmpty()) {
                    IconButton(onClick = { dateQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Filtro rápido por Bloque Horario / Turno
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeSlots.forEach { slot ->
                FilterChip(
                    selected = selectedTimeSlot == slot,
                    onClick = { selectedTimeSlot = slot },
                    label = { Text(slot) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Listado de Turnos Filtrados
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "No se encontraron turnos para ese filtro de fecha/hora.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // key = id + animateItem(): al filtrar o agregar turnos, las tarjetas se mueven suavemente
                items(filteredList, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clickable { onShiftClick(record.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.dateLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Horario",
                                        tint = BrandBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = record.shiftTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(text = "Flujo Total: ${record.totalFlow} personas")
                                Text(
                                    text = "Solo Tránsito (Fuga): ${record.transitOnlyPercentage}%",
                                    color = TransitRed,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toca para ver el detalle del turno",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver detalle",
                                tint = BrandBlue
                            )
                        }
                    }
                }
            }
        }
    }

    // Cerrar el turno borra los registros en vivo: se pide confirmación antes
    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("¿Cerrar el turno?") },
            text = {
                Text(
                    "Se guardará un resumen de $currentShiftPeople personas en el historial " +
                        "y la bitácora del turno quedará vacía."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseConfirm = false
                        viewModel.closeShiftAndSaveToHistory()
                    }
                ) { Text("Cerrar turno") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}
