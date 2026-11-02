package com.example.retailflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.retailflow.ui.theme.SuccessBg
import com.example.retailflow.ui.theme.BuyerGreen
import com.example.retailflow.util.Validators
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(onBackClick: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isEmailValid = Validators.isCorporateEmail(email)
    val emailError = Validators.emailError(email, requireCorporate = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Acceso", style = MaterialTheme.typography.titleMedium) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.MarkEmailRead,
                contentDescription = "Recuperar",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingresa tu correo electrónico corporativo registrado (${Validators.CORPORATE_DOMAIN}). " +
                    "Te enviaremos un código de seguridad de 6 dígitos para restablecer tu acceso.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    sent = false
                },
                label = { Text("Correo Corporativo") },
                placeholder = { Text("ejemplo${Validators.CORPORATE_DOMAIN}") },
                singleLine = true,
                enabled = !isSending,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(emailError)
                    } else if (isEmailValid) {
                        Text("Dominio institucional verificado.", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Corrutina: simula la llamada al servidor sin congelar la pantalla
                    scope.launch {
                        isSending = true
                        delay(1200)
                        isSending = false
                        sent = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isEmailValid && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Enviando...")
                } else {
                    Text("Enviar Código de Seguridad")
                }
            }

            AnimatedVisibility(visible = sent) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SuccessBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "✅ Si el correo está registrado, recibirás el código en unos minutos.",
                        color = BuyerGreen,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a Iniciar Sesión")
            }
        }
    }
}
