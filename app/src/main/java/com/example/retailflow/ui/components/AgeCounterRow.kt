package com.example.retailflow.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.theme.BrandBlue

/**
 * Fila "etiqueta  [-]  N  [+]". No guarda estado propio (state hoisting):
 * recibe el valor y avisa los cambios con lambdas, por eso es reutilizable.
 * El número se anima: sube deslizándose hacia arriba y baja hacia abajo.
 */
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

            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    val goingUp = targetState > initialState
                    (slideInVertically { h -> if (goingUp) h else -h } + fadeIn()) togetherWith
                        (slideOutVertically { h -> if (goingUp) -h else h } + fadeOut())
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                label = "counterAnimation"
            ) { value ->
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (value > 0) BrandBlue else Color.Gray
                )
            }

            FilledIconButton(
                onClick = onIncrement,
                enabled = enabled,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandBlue),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Sumar")
            }
        }
    }
}
