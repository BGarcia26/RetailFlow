package com.example.retailflow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.retailflow.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onActionClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, color = Color.White) },
        navigationIcon = {
            // Antes había un ícono de menú que no hacía nada; ahora solo aparece la flecha cuando sirve.
            if (canNavigateBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver atrás",
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(Icons.Default.Analytics, contentDescription = "Bitácora Demográfica", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BrandBlue)
    )
}
