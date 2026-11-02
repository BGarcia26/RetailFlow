package com.example.retailflow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.ui.theme.TransitRed

/**
 * Barra Compradores (verde, parte llena) vs Solo tránsito (rojo, el fondo).
 * Cuando cambia [buyerPercent] la barra se desliza suavemente al nuevo valor.
 */
@Composable
fun ConversionBar(buyerPercent: Int, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = buyerPercent.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "conversionBar"
    )
    LinearProgressIndicator(
        progress = { animated },
        modifier = modifier.fillMaxWidth(),
        color = BuyerGreen,
        trackColor = TransitRed
    )
}
