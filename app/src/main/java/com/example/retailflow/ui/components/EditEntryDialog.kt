package com.example.retailflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.retailflow.data.AccessZones
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.util.Validators

/** Datos que devuelve el formulario de edición ya validados. */
data class EditedEntry(
    val accessName: String,
    val direction: String,
    val child: Int,
    val young: Int,
    val adult: Int,
    val senior: Int,
    val madePurchase: Boolean
)

/**
 * Formulario (diálogo) para EDITAR un registro. Validaciones:
 *  - cada contador debe ser un número entero entre 0 y 99
 *  - el grupo debe tener al menos 1 persona
 * El botón Guardar solo se habilita cuando todo es válido.
 */
@Composable
fun EditEntryDialog(
    entry: FlowEntryEntity,
    onDismiss: () -> Unit,
    onSave: (EditedEntry) -> Unit
) {
    var access by remember { mutableStateOf(entry.accessName) }
    var direction by remember { mutableStateOf(entry.direction) }
    var purchase by remember { mutableStateOf(entry.madePurchase) }

    // Los contadores se guardan como TEXTO para poder validar lo que el usuario escribe
    var child by remember { mutableStateOf(entry.childCount.toString()) }
    var young by remember { mutableStateOf(entry.youngCount.toString()) }
    var adult by remember { mutableStateOf(entry.adultCount.toString()) }
    var senior by remember { mutableStateOf(entry.seniorCount.toString()) }

    val parsed = listOf(child, young, adult, senior).map { Validators.parseCount(it) }
    val allNumbersValid = parsed.none { it == null }
    val total = parsed.sumOf { it ?: 0 }
    val groupEmpty = allNumbersValid && total == 0
    val canSave = allNumbersValid && total > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar registro") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Punto de acceso", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                AccessZones.all.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { zone ->
                            FilterChip(
                                selected = access == zone.name,
                                onClick = { access = zone.name },
                                label = { Text(zone.shortName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Text("Sentido", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Entrada", "Salida").forEach { dir ->
                        FilterChip(
                            selected = direction == dir,
                            onClick = { direction = dir },
                            label = { Text(dir) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("Integrantes por edad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                CountField("Niños (< 14)", child) { child = it }
                CountField("Jóvenes (14 - 25)", young) { young = it }
                CountField("Adultos (26 - 59)", adult) { adult = it }
                CountField("Seniors (60+)", senior) { senior = it }

                if (groupEmpty) {
                    Text(
                        text = "El grupo debe tener al menos 1 persona",
                        color = TransitRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (purchase) "🛍️ Realizó compra" else "🚶 Solo tránsito")
                    Switch(checked = purchase, onCheckedChange = { purchase = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        EditedEntry(
                            accessName = access,
                            direction = direction,
                            child = parsed[0] ?: 0,
                            young = parsed[1] ?: 0,
                            adult = parsed[2] ?: 0,
                            senior = parsed[3] ?: 0,
                            madePurchase = purchase
                        )
                    )
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun CountField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        // Solo se aceptan dígitos y máximo 2 caracteres (0..99)
        onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) onChange(it) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = Validators.countError(value) != null,
        supportingText = { Validators.countError(value)?.let { Text(it) } },
        modifier = Modifier.fillMaxWidth()
    )
}
