package com.example.retailflow.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.example.retailflow.util.ImageUtils

/**
 * Foto de perfil circular. Si no hay foto (o falla la carga) muestra el ícono por defecto.
 * La imagen se carga en segundo plano (Dispatchers.IO dentro de ImageUtils) con produceState,
 * y aparece con un fundido (Crossfade).
 */
@Composable
fun UserAvatar(photoPath: String?, size: Dp, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = photoPath) {
        value = photoPath?.let { ImageUtils.loadBitmap(it)?.asImageBitmap() }
    }

    Crossfade(targetState = bitmap, modifier = modifier, label = "avatarCrossfade") { image ->
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(size),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
