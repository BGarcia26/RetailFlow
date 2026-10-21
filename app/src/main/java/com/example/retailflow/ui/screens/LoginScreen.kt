package com.example.retailflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.retailflow.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLoginClick: () -> Unit,
    onRecoverClick: () -> Unit
) {
    val state by vm.loginState.collectAsStateWithLifecycle()
    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Rincón Sur", style = MaterialTheme.typography.headlineLarge)
            Text(text = "Acceso Colaboradores & Analítica", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = { Text("Correo Corporativo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Usuario") },
                singleLine = true,
                isError = state.emailError != null, // Activa el borde rojo si hay error
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (state.emailError != null) {
                        Text(state.emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.pass,
                onValueChange = vm::onPassChange,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Candado") },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
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
            TextButton(
                onClick = onRecoverClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Ingresar")
            }
        }
    }
}