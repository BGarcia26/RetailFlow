package com.example.retailflow.ui.screens

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

    // Lista filtrada reactivamente
    val filteredList = historyList.filter { record ->
        val matchesDate = record.dateLabel.contains(dateQuery, ignoreCase = true) ||
                record.shiftTime.contains(dateQuery, ignoreCase = true)
        val matchesSlot = selectedTimeSlot == "Todos" ||
                record.shiftTime.startsWith(selectedTimeSlot, ignoreCase = true)
        matchesDate && matchesSlot
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tarjeta para archivar el turno en curso si hay datos capturados
        if (currentShiftPeople > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Turno Actual en Curso: $currentShiftPeople personas registradas",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.closeShiftAndSaveToHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A89))
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
                items(filteredList) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        tint = Color(0xFF455A89),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = record.shiftTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF455A89),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(text = "Flujo Total: ${record.totalFlow} personas")
                                Text(
                                    text = "Solo Tránsito (Fuga): ${record.transitOnlyPercentage}%",
                                    color = Color(0xFFD32F2F),
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
                                tint = Color(0xFF455A89)
                            )
                        }
                    }
                }
            }
        }
    }
}