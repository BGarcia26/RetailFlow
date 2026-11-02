package com.example.retailflow.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.components.AppearAnimated
import com.example.retailflow.ui.components.ConversionBar
import com.example.retailflow.ui.theme.BrandBlue
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.ui.viewmodel.HistoryViewModel
import com.example.retailflow.util.FlowCalculations
import com.example.retailflow.util.ShiftReport

private fun people(count: Int) = FlowCalculations.countLabel(count, "persona", "personas")

private fun records(count: Int) = FlowCalculations.countLabel(count, "registro", "registros")

@Composable
fun ShiftDetailScreen(
    shiftId: Int,
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()
    val context = LocalContext.current
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
        // Botones superiores: volver y compartir
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver")
            }
            // Recurso nativo: abre el menú "Compartir" de Android (WhatsApp, correo, etc.)
            FilledTonalButton(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, ShiftReport.subject(shift))
                        putExtra(Intent.EXTRA_TEXT, ShiftReport.build(shift))
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartir reporte del turno"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir reporte")
            }
        }

        // 1. Encabezado de Fecha y Horario del Turno
        AppearAnimated(index = 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandBlue)
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
        }

        // 2. Conversión Comercial (Compradores vs Fuga)
        AppearAnimated(index = 1) {
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
                        text = "Flujo Total Registrado: ${people(shift.totalFlow)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🛍️ Compradores: ${shift.buyersCount} ($buyerPct%)",
                            color = BuyerGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🚶 Fuga: $transitCount (${shift.transitOnlyPercentage}%)",
                            color = TransitRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ConversionBar(buyerPercent = buyerPct)
                }
            }
        }

        // 3. Desglose por Rango Etario
        AppearAnimated(index = 2) {
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
                    Text("• Niños (< 14 años): ${people(shift.childCount)}")
                    Text("• Jóvenes (14 - 25 años): ${people(shift.youngCount)}")
                    Text("• Adultos (26 - 59 años): ${people(shift.adultCount)}")
                    Text("• Seniors (60+ años): ${people(shift.seniorCount)}")
                }
            }
        }

        // 4. Desglose por Tipo de Compañía
        AppearAnimated(index = 3) {
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
                    Text("• Visitantes Solos: ${records(shift.soloCount)}")
                    Text("• Parejas: ${records(shift.coupleCount)}")
                    Text("• Familias / Grupos (3+): ${records(shift.groupCount)}")
                }
            }
        }

        // 5. Desglose por Puerta de Acceso
        AppearAnimated(index = 4) {
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
}
