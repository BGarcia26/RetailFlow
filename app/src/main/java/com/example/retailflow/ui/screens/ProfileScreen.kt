package com.example.retailflow.ui.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.retailflow.ui.components.AppearAnimated
import com.example.retailflow.ui.components.UserAvatar
import com.example.retailflow.ui.viewmodel.AuthViewModel
import com.example.retailflow.util.ImageUtils
import kotlinx.coroutines.launch

/**
 * Perfil del usuario logueado. La foto usa recursos nativos del dispositivo:
 *  - Cámara: ActivityResultContracts.TakePicture + FileProvider
 *  - Galería: ActivityResultContracts.PickVisualMedia (selector de fotos del sistema)
 * Ninguna de las dos pide permisos peligrosos en el Manifest.
 */
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogoutClick: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPhotoOptions by remember { mutableStateOf(false) }

    // ---- Galería ----
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val current = user
        if (uri != null && current != null) {
            scope.launch {
                val path = ImageUtils.saveAvatar(context, uri, current.id)
                if (path != null) {
                    authViewModel.setProfilePhoto(path)
                } else {
                    snackbarHostState.showSnackbar("No se pudo cargar la imagen")
                }
            }
        }
    }

    // ---- Cámara ----
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val current = user
        if (success && current != null) {
            scope.launch {
                val path = ImageUtils.saveAvatar(context, ImageUtils.cameraTempUri(context), current.id)
                ImageUtils.deleteCameraTemp(context)
                if (path != null) {
                    authViewModel.setProfilePhoto(path)
                } else {
                    snackbarHostState.showSnackbar("No se pudo guardar la foto")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppearAnimated(index = 0) {
                Box(modifier = Modifier.size(140.dp)) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .clickable { showPhotoOptions = true }
                    ) {
                        UserAvatar(photoPath = user?.photoPath, size = 140.dp)
                    }
                    // Insignia de cámara: indica que la foto se puede cambiar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Cambiar foto",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppearAnimated(index = 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = user?.fullName ?: "Supervisor",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Supervisor de Monitoreo & Operaciones",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Sucursal Mall Plaza Sur",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AppearAnimated(index = 2) {
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Foto de perfil") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            try {
                                cameraLauncher.launch(ImageUtils.cameraTempUri(context))
                            } catch (e: ActivityNotFoundException) {
                                scope.launch { snackbarHostState.showSnackbar("No se encontró una app de cámara") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("📷  Tomar foto") }

                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🖼️  Elegir de la galería") }

                    if (user?.photoPath != null) {
                        TextButton(
                            onClick = {
                                showPhotoOptions = false
                                authViewModel.setProfilePhoto(null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("🗑️  Quitar foto") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptions = false }) { Text("Cancelar") }
            }
        )
    }
}
