package com.example.retailflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.viewmodel.HistoryViewModel

@Composable
fun ShiftDetailScreen(
    shiftId: Int,
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()
    val shift = historyList.find { it.id == shiftId }

    if (shift == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cargando información del turno...")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBackClick) {
                Text("Volver")
            }
        }
        return
    }

    val buyerPct = 100 - shift.transitOnlyPercentage
    val transitCount = shift.totalFlow - shift.buyersCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Botón superior para regresar al Historial
        OutlinedButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver al Historial")
        }

        // 1. Encabezado de Fecha y Horario del Turno
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF455A89))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reporte Detallado de Turno #${shift.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = shift.dateLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Horario: ${shift.shiftTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        // 2. Conversión Comercial (Compradores vs Fuga)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Conversión y Flujo del Turno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Flujo Total Registrado: ${shift.totalFlow} personas",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🛍️ Compradores: ${shift.buyersCount} ($buyerPct%)",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🚶 Fuga: $transitCount (${shift.transitOnlyPercentage}%)",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { buyerPct / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2E7D32),
                    trackColor = Color(0xFFD32F2F)
                )
            }
        }

        // 3. Desglose por Rango Etario
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Distribución por Edad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Niños (< 14 años): ${shift.childCount} personas")
                Text("• Jóvenes (14 - 25 años): ${shift.youngCount} personas")
                Text("• Adultos (26 - 59 años): ${shift.adultCount} personas")
                Text("• Seniors (60+ años): ${shift.seniorCount} personas")
            }
        }

        // 4. Desglose por Tipo de Compañía
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Modalidad de Visita (Grupos)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Visitantes Solos: ${shift.soloCount} registros")
                Text("• Parejas: ${shift.coupleCount} registros")
                Text("• Familias / Grupos (3+): ${shift.groupCount} registros")
            }
        }

        // 5. Desglose por Puerta de Acceso
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Flujo por Punto de Acceso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = shift.accessSummary)
            }
        }
    }
}