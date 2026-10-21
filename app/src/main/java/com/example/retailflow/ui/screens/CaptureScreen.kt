package com.example.retailflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.viewmodel.HistoryViewModel

@Composable
fun CaptureScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isSampling by viewModel.isSamplingActive.collectAsState()

    val accessList = listOf(
        "Acceso 1 - Conexión Mall",
        "Acceso 2 - Estacionamiento",
        "Acceso 3 - Calle Externa",
        "Acceso 4 - Pasarela Metro"
    )
    var selectedAccess by remember { mutableStateOf(accessList[0]) }
    var direction by remember { mutableStateOf("Salida") }

    // Contadores individuales para permitir familias/grupos mixtos
    var childCount by remember { mutableIntStateOf(0) }
    var youngCount by remember { mutableIntStateOf(0) }
    var adultCount by remember { mutableIntStateOf(1) } // Por defecto 1 adulto listo para registrar
    var seniorCount by remember { mutableIntStateOf(0) }

    var lastSavedFeedback by remember { mutableStateOf("") }

    val totalGroupPeople = childCount + youngCount + adultCount + seniorCount
    val autoCompanionLabel = when {
        totalGroupPeople == 0 -> "Selecciona al menos 1 persona"
        totalGroupPeople == 1 -> "👤 Visita Individual (Solo - 1 pers.)"
        totalGroupPeople == 2 -> "👥 Pareja / Dúo (2 pers.)"
        else -> "👨‍👩‍👧‍👦 Grupo / Familia ($totalGroupPeople pers.)"
    }

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    fun resetCountersAfterSave() {
        childCount = 0
        youngCount = 0
        adultCount = 0
        seniorCount = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Control de Turno / Muestreo
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSampling) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSampling) "🟢 Turno de Muestreo Activo" else "⏸️ Turno Detenido (Consola Bloqueada)",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSampling) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tiempo: $formattedTime | Capturados: ${entries.sumOf { it.groupSize }} pers.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isSampling) {
                        Text(
                            text = "Presiona ▶️ para habilitar el ingreso de personas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
                FilledIconButton(
                    onClick = { viewModel.toggleSamplingTimer() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSampling) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                ) {
                    Icon(
                        imageVector = if (isSampling) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Iniciar/Detener Muestreo"
                    )
                }
            }
        }

        // 2. Selector de Puerta / Acceso
        Text("1. Punto de Acceso", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            accessList.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { access ->
                        FilterChip(
                            selected = selectedAccess == access,
                            onClick = { selectedAccess = access },
                            enabled = isSampling,
                            label = { Text(access.substringBefore(" -")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Sentido del Flujo
        Text("2. Sentido de Circulación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Entrada", "Salida").forEach { dir ->
                FilterChip(
                    selected = direction == dir,
                    onClick = { direction = dir },
                    enabled = isSampling,
                    label = { Text(if (dir == "Entrada") "➡️ Entra a Tienda" else "⬅️ Sale de Tienda") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Armador de Grupo y Edades (Soporta familias mixtas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. Integrantes del Grupo por Edad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = { resetCountersAfterSave() },
                enabled = isSampling && totalGroupPeople > 0
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpiar (0)")
            }
        }

        AgeCounterRow(
            label = "👶 Niños (< 14 años)",
            count = childCount,
            enabled = isSampling,
            onDecrement = { if (childCount > 0) childCount-- },
            onIncrement = { childCount++ }
        )
        AgeCounterRow(
            label = "👦 Jóvenes (14 - 25 años)",
            count = youngCount,
            enabled = isSampling,
            onDecrement = { if (youngCount > 0) youngCount-- },
            onIncrement = { youngCount++ }
        )
        AgeCounterRow(
            label = "🧑 Adultos (26 - 59 años)",
            count = adultCount,
            enabled = isSampling,
            onDecrement = { if (adultCount > 0) adultCount-- },
            onIncrement = { adultCount++ }
        )
        AgeCounterRow(
            label = "👴 Seniors (60+ años)",
            count = seniorCount,
            enabled = isSampling,
            onDecrement = { if (seniorCount > 0) seniorCount-- },
            onIncrement = { seniorCount++ }
        )

        // Resumen automático de clasificación (Solo / Pareja / Familia)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = autoCompanionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (totalGroupPeople > 0) Color(0xFF455A89) else Color.Gray
                )
            }
        }

        HorizontalDivider()

        // 5. Botones 1-Tap para Guardar
        Text(
            text = if (isSampling) "4. Resultado (Guarda el grupo y limpia)" else "4. Resultado (Inicia el turno arriba)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.registerGroupFlow(
                        accessName = selectedAccess,
                        direction = direction,
                        childCount = childCount,
                        youngCount = youngCount,
                        adultCount = adultCount,
                        seniorCount = seniorCount,
                        madePurchase = true
                    )
                    lastSavedFeedback = "✅ Guardado ($totalGroupPeople pers. CON COMPRA)"
                    resetCountersAfterSave()
                },
                enabled = isSampling && totalGroupPeople > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
            ) {
                Text("🛍️ CON COMPRA", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.registerGroupFlow(
                        accessName = selectedAccess,
                        direction = direction,
                        childCount = childCount,
                        youngCount = youngCount,
                        adultCount = adultCount,
                        seniorCount = seniorCount,
                        madePurchase = false
                    )
                    lastSavedFeedback = "⚠️ Guardado ($totalGroupPeople pers. SOLO TRÁNSITO)"
                    resetCountersAfterSave()
                },
                enabled = isSampling && totalGroupPeople > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
            ) {
                Text("🚶 SOLO TRÁNSITO", fontWeight = FontWeight.Bold)
            }
        }

        if (lastSavedFeedback.isNotEmpty()) {
            Text(
                text = lastSavedFeedback,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF455A89),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AgeCounterRow(
    label: String,
    count: Int,
    enabled: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedIconButton(
                onClick = onDecrement,
                enabled = enabled && count > 0,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Restar")
            }
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) Color(0xFF455A89) else Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            FilledIconButton(
                onClick = onIncrement,
                enabled = enabled,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF455A89)),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Sumar")
            }
        }
    }
}