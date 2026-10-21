package com.example.retailflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onEmailChange(value: String) {
        val error = if (value.isNotEmpty() && !value.contains("@")) {
            "Correo inválido. Usa tu cuenta corporativa"
        } else null

        _loginState.update { it.copy(email = value, emailError = error) }
        recomputeCanSubmit()
    }

    fun onPassChange(value: String) {
        _loginState.update { it.copy(pass = value) }
        recomputeCanSubmit()
    }

    private fun recomputeCanSubmit() {
        val s = _loginState.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _loginState.update { it.copy(canSubmit = can) }
    }
}