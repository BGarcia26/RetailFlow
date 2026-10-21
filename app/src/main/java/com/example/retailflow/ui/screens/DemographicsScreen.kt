package com.example.retailflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.viewmodel.HistoryViewModel

@Composable
fun DemographicsScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()
    val totalPeople = entries.sumOf { it.groupSize }

    val childCount = entries.sumOf { it.childCount }
    val youngCount = entries.sumOf { it.youngCount }
    val adultCount = entries.sumOf { it.adultCount }
    val seniorCount = entries.sumOf { it.seniorCount }

    val soloCount = entries.filter { it.companionType == "Solo" }.count()
    val coupleCount = entries.filter { it.companionType == "Pareja" }.count()
    val groupCount = entries.filter { it.companionType == "Grupo/Familia" }.count()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen Demográfico del Turno", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Personas Registradas: $totalPeople")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Por Edad: Niños: $childCount | Jóvenes: $youngCount | Adultos: $adultCount | Seniors: $seniorCount")
                    Text("Por Grupo: Solos: $soloCount | Parejas: $coupleCount | Familias: $groupCount")
                }
            }
        }

        item {
            Text(
                text = "Bitácora de Registros Individuales (${entries.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (entries.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay registros manuales en este turno. Ve a la pestaña 'Capturar' para ingresar personas.",
                    color = Color.Gray
                )
            }
        } else {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${entry.accessName} (${entry.direction})",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Edad: ${entry.ageGroup} | ${entry.companionType} (${entry.groupSize} pers.)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (entry.madePurchase) "🛍️ Comprador (${entry.timestamp})" else "🚶 Solo Tránsito (${entry.timestamp})",
                                color = if (entry.madePurchase) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar registro",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}