package com.example.retailflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.retailflow.ui.components.AppearAnimated
import com.example.retailflow.ui.theme.BrandBlue
import com.example.retailflow.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRecoverClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val state by vm.loginState.collectAsStateWithLifecycle()
    var showPass by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Se usa tanto en el botón como en la tecla "Listo" del teclado
    val doLogin = {
        focusManager.clearFocus()
        vm.login()
    }

    // Navega la pantalla actual, que siempre tiene el NavController vivo aunque se haya girado
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) {
            vm.onLoginHandled()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppearAnimated(index = 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Rincón Sur", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = "Acceso Colaboradores & Analítica",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            AppearAnimated(index = 1) {
                Column {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Correo Corporativo") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Usuario") },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        isError = state.emailError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            state.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.pass,
                        onValueChange = vm::onPassChange,
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Candado") },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { if (state.canSubmit) doLogin() }),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Alternar visibilidad"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Mensaje de error del login: aparece/desaparece con animación
            AnimatedVisibility(visible = state.authError != null) {
                Text(
                    text = state.authError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            TextButton(
                onClick = onRecoverClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { doLogin() },
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
                    Text("Verificando...")
                } else {
                    Text("Ingresar")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Crear cuenta")
            }
        }
    }
}
