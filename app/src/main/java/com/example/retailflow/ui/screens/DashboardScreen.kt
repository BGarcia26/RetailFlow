package com.example.retailflow.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import com.example.retailflow.data.AccessZones
import com.example.retailflow.ui.components.AppearAnimated
import com.example.retailflow.ui.components.ConversionBar
import com.example.retailflow.ui.theme.BrandBlue
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.ui.viewmodel.HistoryViewModel
import com.example.retailflow.util.FlowCalculations

/**
 * Dashboard con datos REALES: por cada acceso suma
 *   (todos los turnos cerrados, desde la tabla shift_access_stats)
 * + (los registros del turno en curso, desde flow_entries).
 */
@Composable
fun DashboardScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()
    val history by viewModel.historyList.collectAsState()
    val accessTotals by viewModel.accessTotals.collectAsState()

    val grandTotal = accessTotals.sumOf { it.totalPeople } + entries.sumOf { it.groupSize }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "resumen") {
            AppearAnimated(index = 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen acumulado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${history.size} turnos cerrados + turno en curso",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Total de personas registradas: $grandTotal",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        items(AccessZones.all, key = { it.name }) { zone ->
            val index = AccessZones.all.indexOf(zone) + 1 // el 0 es la tarjeta de resumen

            // Turnos ya cerrados (Room: suma de shift_access_stats para este acceso)
            val closed = accessTotals.firstOrNull { it.accessName == zone.name }
            val closedTotal = closed?.totalPeople ?: 0
            val closedBuyers = closed?.buyersCount ?: 0

            // Turno en curso (todavía no cerrado)
            val zoneEntries = entries.filter { it.accessName == zone.name }
            val addedTotal = zoneEntries.sumOf { it.groupSize }
            val addedBuyers = zoneEntries.filter { it.madePurchase }.sumOf { it.groupSize }

            val totalFlow = closedTotal + addedTotal
            val totalBuyers = closedBuyers + addedBuyers
            val hasData = totalFlow > 0
            val buyerPct = FlowCalculations.percent(totalBuyers, totalFlow)
            val transitPct = if (hasData) 100 - buyerPct else 0

            // El total "cuenta" hasta su nuevo valor cuando se registra gente
            val animatedTotal by animateIntAsState(
                targetValue = totalFlow,
                animationSpec = tween(600),
                label = "zoneTotal"
            )

            AppearAnimated(index = index) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    color = BrandBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total Ingresos: $animatedTotal",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "En turnos cerrados: $closedTotal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (hasData) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Compradores: $buyerPct%", color = BuyerGreen)
                                Text("Solo Tránsito: $transitPct%", color = TransitRed)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            ConversionBar(buyerPercent = buyerPct)
                        } else {
                            Text(
                                text = "Sin datos registrados todavía",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
