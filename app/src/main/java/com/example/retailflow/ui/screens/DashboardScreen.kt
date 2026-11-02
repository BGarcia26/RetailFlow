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

@Composable
fun DashboardScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(AccessZones.all, key = { it.name }) { zone ->
            val index = AccessZones.all.indexOf(zone)
            val zoneEntries = entries.filter { it.accessName == zone.name }
            val addedTotal = zoneEntries.sumOf { it.groupSize }
            val addedBuyers = zoneEntries.filter { it.madePurchase }.sumOf { it.groupSize }

            val totalFlow = zone.baseTotal + addedTotal
            val totalBuyers = zone.baseBuyers + addedBuyers
            val buyerPct = FlowCalculations.percent(totalBuyers, totalFlow)
            val transitPct = 100 - buyerPct

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
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Compradores: $buyerPct%", color = BuyerGreen)
                            Text("Solo Tránsito: $transitPct%", color = TransitRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        ConversionBar(buyerPercent = buyerPct)
                    }
                }
            }
        }
    }
}
