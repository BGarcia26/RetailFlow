package com.example.retailflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.retailflow.ui.components.AppearAnimated
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.ui.theme.TransitRed
import com.example.retailflow.ui.theme.WarnOrange
import com.example.retailflow.ui.viewmodel.AuthViewModel
import com.example.retailflow.util.Validators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onBackClick: () -> Unit,
    onRegistered: () -> Unit
) {
    val state by vm.registerState.collectAsStateWithLifecycle()
    var showPass by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Al salir de la pantalla se limpia el formulario (no queda la clave escrita en memoria)
    DisposableEffect(Unit) {
        onDispose { vm.clearRegisterForm() }
    }

    val doRegister = {
        focusManager.clearFocus()
        vm.register(onRegistered)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al Login")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            AppearAnimated(index = 0) {
                Column {
                    Text("Registra tu cuenta", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Usa tu correo corporativo (${Validators.CORPORATE_DOMAIN}). " +
                            "La contraseña debe tener al menos ${Validators.MIN_PASSWORD_LENGTH} caracteres, " +
                            "una mayúscula y un número.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AppearAnimated(index = 1) {
                Column {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = vm::onRegisterNameChange,
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        isError = state.nameError != null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        supportingText = { state.nameError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onRegisterEmailChange,
                        label = { Text("Correo corporativo") },
                        placeholder = { Text("nombre${Validators.CORPORATE_DOMAIN}") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        isError = state.emailError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        supportingText = { state.emailError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.pass,
                        onValueChange = vm::onRegisterPassChange,
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        isError = state.passError != null,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Alternar visibilidad"
                                )
                            }
                        },
                        supportingText = { state.passError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Indicador de seguridad: aparece al escribir y anima ancho y color
                    AnimatedVisibility(visible = state.pass.isNotEmpty()) {
                        PasswordStrengthIndicator(strength = state.strength)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.confirm,
                        onValueChange = vm::onRegisterConfirmChange,
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        isError = state.confirmError != null,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { if (state.canSubmit) doRegister() }),
                        supportingText = { state.confirmError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error devuelto por Room/Repository (ej: correo ya registrado)
            AnimatedVisibility(visible = state.registerError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = state.registerError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { doRegister() },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Crear cuenta")
                }
            }

            TextButton(
                onClick = onBackClick,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: Int) {
    val progress by animateFloatAsState(
        targetValue = strength / 4f,
        animationSpec = tween(400),
        label = "strengthProgress"
    )
    val color by animateColorAsState(
        targetValue = when (strength) {
            0, 1 -> TransitRed
            2 -> WarnOrange
            3 -> Color(0xFF9E9D24)
            else -> BuyerGreen
        },
        animationSpec = tween(400),
        label = "strengthColor"
    )
    val label = when (strength) {
        0, 1 -> "Débil"
        2 -> "Media"
        3 -> "Buena"
        else -> "Fuerte"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Seguridad: $label",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
