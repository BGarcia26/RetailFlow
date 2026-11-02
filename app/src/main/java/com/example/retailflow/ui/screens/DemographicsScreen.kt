package com.example.retailflow.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.ui.components.EditEntryDialog
import com.example.retailflow.ui.theme.BrandBlue
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.ui.viewmodel.HistoryViewModel
import com.example.retailflow.util.FlowCalculations
import kotlinx.coroutines.launch

@Composable
fun DemographicsScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entriesList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editingEntry by remember { mutableStateOf<FlowEntryEntity?>(null) }

    val totalPeople = entries.sumOf { it.groupSize }
    val animatedTotal by animateIntAsState(
        targetValue = totalPeople,
        animationSpec = tween(500),
        label = "totalPeople"
    )

    val childCount = entries.sumOf { it.childCount }
    val youngCount = entries.sumOf { it.youngCount }
    val adultCount = entries.sumOf { it.adultCount }
    val seniorCount = entries.sumOf { it.seniorCount }

    val soloCount = entries.count { it.companionType == FlowCalculations.SOLO }
    val coupleCount = entries.count { it.companionType == FlowCalculations.PAREJA }
    val groupCount = entries.count { it.companionType == FlowCalculations.GRUPO }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Text("Total Personas Registradas: $animatedTotal")
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
                // key = id: Compose identifica cada fila y anima su entrada, salida y reordenamiento
                items(entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
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
                                    color = if (entry.madePurchase) BuyerGreen else TransitRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // UPDATE
                            IconButton(onClick = { editingEntry = entry }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar registro",
                                    tint = BrandBlue
                                )
                            }
                            // DELETE (con opción de deshacer)
                            IconButton(
                                onClick = {
                                    viewModel.deleteEntry(entry)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Registro eliminado",
                                            actionLabel = "Deshacer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreEntry(entry)
                                        }
                                    }
                                }
                            ) {
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSave = { edited ->
                viewModel.updateEntry(
                    original = entry,
                    accessName = edited.accessName,
                    direction = edited.direction,
                    childCount = edited.child,
                    youngCount = edited.young,
                    adultCount = edited.adult,
                    seniorCount = edited.senior,
                    madePurchase = edited.madePurchase
                )
                editingEntry = null
                scope.launch { snackbarHostState.showSnackbar("Registro actualizado") }
            }
        )
    }
}
