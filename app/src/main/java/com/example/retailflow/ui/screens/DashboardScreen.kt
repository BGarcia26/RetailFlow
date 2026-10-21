package com.example.retailflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.viewmodel.HistoryViewModel

data class AccessZoneBase(
    val name: String,
    val baseTotal: Int,
    val baseBuyers: Int
)

@Composable
fun DashboardScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()

    val baseZones = listOf(
        AccessZoneBase("Acceso 1 - Conexión Mall", 2840, 994),
        AccessZoneBase("Acceso 2 - Estacionamiento", 1960, 1000),
        AccessZoneBase("Acceso 3 - Calle Externa", 1240, 533),
        AccessZoneBase("Acceso 4 - Pasarela Metro", 850, 374)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(baseZones) { zone ->
            val zoneEntries = entries.filter { it.accessName == zone.name }
            val addedTotal = zoneEntries.sumOf { it.groupSize }
            val addedBuyers = zoneEntries.filter { it.madePurchase }.sumOf { it.groupSize }

            val totalFlow = zone.baseTotal + addedTotal
            val totalBuyers = zone.baseBuyers + addedBuyers
            val buyerPct = if (totalFlow > 0) ((totalBuyers.toFloat() / totalFlow.toFloat()) * 100).toInt() else 0
            val transitPct = 100 - buyerPct

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (addedTotal > 0) {
                            Text(
                                text = "+$addedTotal hoy",
                                color = Color(0xFF455A89),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Ingresos: $totalFlow",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Compradores: $buyerPct%", color = Color(0xFF2E7D32))
                        Text("Solo Tránsito: $transitPct%", color = Color(0xFFD32F2F))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { buyerPct / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2E7D32),
                        trackColor = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}