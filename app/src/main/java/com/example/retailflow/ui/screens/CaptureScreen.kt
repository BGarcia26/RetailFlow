package com.example.retailflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.data.AccessZones
import com.example.retailflow.ui.components.AgeCounterRow
import com.example.retailflow.ui.theme.BrandBlue
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.ui.theme.SuccessBg
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.ui.theme.WarnBg
import com.example.retailflow.ui.theme.WarnOrange
import com.example.retailflow.ui.viewmodel.HistoryViewModel
import com.example.retailflow.util.FlowCalculations
import kotlinx.coroutines.delay

@Composable
fun CaptureScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isSampling by viewModel.isSamplingActive.collectAsState()
    val haptic = LocalHapticFeedback.current

    val accessZones = AccessZones.all
    var selectedAccess by remember { mutableStateOf(accessZones[0].name) }
    var direction by remember { mutableStateOf("Salida") }

    // Contadores individuales para permitir familias/grupos mixtos
    var childCount by remember { mutableIntStateOf(0) }
    var youngCount by remember { mutableIntStateOf(0) }
    var adultCount by remember { mutableIntStateOf(1) } // Por defecto 1 adulto listo para registrar
    var seniorCount by remember { mutableIntStateOf(0) }

    // Mensaje de confirmación que se muestra 3 segundos y desaparece solo
    var feedbackText by remember { mutableStateOf("") }
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(feedbackTick) {
        if (feedbackTick > 0) {
            delay(3000)
            feedbackVisible = false
        }
    }

    val totalGroupPeople = childCount + youngCount + adultCount + seniorCount
    val autoCompanionLabel = when {
        totalGroupPeople == 0 -> "Selecciona al menos 1 persona"
        totalGroupPeople == 1 -> "👤 Visita Individual (Solo - 1 pers.)"
        totalGroupPeople == 2 -> "👥 Pareja / Dúo (2 pers.)"
        else -> "👨‍👩‍👧‍👦 Grupo / Familia ($totalGroupPeople pers.)"
    }

    // ---- Animaciones del panel de turno ----
    val cardColor by animateColorAsState(
        targetValue = if (isSampling) SuccessBg else WarnBg,
        animationSpec = tween(400),
        label = "shiftCardColor"
    )
    val statusColor by animateColorAsState(
        targetValue = if (isSampling) BuyerGreen else WarnOrange,
        animationSpec = tween(400),
        label = "shiftStatusColor"
    )
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    fun clearCounters() {
        childCount = 0
        youngCount = 0
        adultCount = 0
        seniorCount = 0
    }

    fun saveGroup(madePurchase: Boolean) {
        val saved = totalGroupPeople
        viewModel.registerGroupFlow(
            accessName = selectedAccess,
            direction = direction,
            childCount = childCount,
            youngCount = youngCount,
            adultCount = adultCount,
            seniorCount = seniorCount,
            madePurchase = madePurchase
        )
        feedbackText = if (madePurchase) "✅ Guardado ($saved pers. CON COMPRA)"
        else "⚠️ Guardado ($saved pers. SOLO TRÁNSITO)"
        feedbackVisible = true
        feedbackTick++
        haptic.performHapticFeedback(HapticFeedbackType.LongPress) // vibración al guardar
        // Listo para la siguiente persona: 1 adulto por defecto
        clearCounters()
        adultCount = 1
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
            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Punto que "late" mientras el turno está activo
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .alpha(if (isSampling) pulse else 1f)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isSampling) "Turno de Muestreo Activo" else "Turno Detenido (Consola Bloqueada)",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Tiempo: ${FlowCalculations.formatTimer(timerSeconds)} | Capturados: ${entries.sumOf { it.groupSize }} pers.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = !isSampling) {
                        Text(
                            text = "Presiona ▶️ para habilitar el ingreso de personas",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarnOrange
                        )
                    }
                }
                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleSamplingTimer()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSampling) TransitRed else BuyerGreen
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
            accessZones.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { zone ->
                        FilterChip(
                            selected = selectedAccess == zone.name,
                            onClick = { selectedAccess = zone.name },
                            enabled = isSampling,
                            label = { Text(zone.shortName) },
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
                onClick = { clearCounters() },
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
            onIncrement = {
                childCount++
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        )
        AgeCounterRow(
            label = "👦 Jóvenes (14 - 25 años)",
            count = youngCount,
            enabled = isSampling,
            onDecrement = { if (youngCount > 0) youngCount-- },
            onIncrement = {
                youngCount++
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        )
        AgeCounterRow(
            label = "🧑 Adultos (26 - 59 años)",
            count = adultCount,
            enabled = isSampling,
            onDecrement = { if (adultCount > 0) adultCount-- },
            onIncrement = {
                adultCount++
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        )
        AgeCounterRow(
            label = "👴 Seniors (60+ años)",
            count = seniorCount,
            enabled = isSampling,
            onDecrement = { if (seniorCount > 0) seniorCount-- },
            onIncrement = {
                seniorCount++
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
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
                    color = if (totalGroupPeople > 0) BrandBlue else Color.Gray
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
                onClick = { saveGroup(madePurchase = true) },
                enabled = isSampling && totalGroupPeople > 0,
                colors = ButtonDefaults.buttonColors(containerColor = BuyerGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
            ) {
                Text("🛍️ CON COMPRA", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { saveGroup(madePurchase = false) },
                enabled = isSampling && totalGroupPeople > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TransitRed),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
            ) {
                Text("🚶 SOLO TRÁNSITO", fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(visible = feedbackVisible) {
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.bodySmall,
                color = BrandBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
