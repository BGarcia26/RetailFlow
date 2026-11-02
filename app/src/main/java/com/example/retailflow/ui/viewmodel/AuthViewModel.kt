package com.example.retailflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailflow.data.local.UserEntity
import com.example.retailflow.data.repository.UserRepository
import com.example.retailflow.util.ImageUtils
import com.example.retailflow.util.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val authError: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val pass: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val strength: Int = 0,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val registerError: String? = null
)

/**
 * Maneja login, registro y la sesión actual (quién está logueado).
 * Se crea UNA vez a nivel de Activity para que Login, Registro y Perfil compartan la sesión.
 */
class AuthViewModel(private val repo: UserRepository) : ViewModel() {

    // ---------- LOGIN ----------
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private fun canLogin(email: String, pass: String, emailError: String?) =
        emailError == null && email.isNotBlank() && pass.isNotBlank()

    fun onEmailChange(value: String) = _loginState.update { s ->
        val error = Validators.emailError(value)
        s.copy(
            email = value,
            emailError = error,
            authError = null,
            canSubmit = canLogin(value, s.pass, error) && !s.isSubmitting
        )
    }

    fun onPassChange(value: String) = _loginState.update { s ->
        s.copy(
            pass = value,
            authError = null,
            canSubmit = canLogin(s.email, value, s.emailError) && !s.isSubmitting
        )
    }

    /** Valida contra Room. La corrutina evita bloquear la UI mientras se consulta y se calcula el hash. */
    fun login(onSuccess: () -> Unit) {
        val s = _loginState.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _loginState.update { it.copy(isSubmitting = true, canSubmit = false, authError = null) }
            delay(600) // pequeña espera para que se vea el indicador de carga
            val user = repo.login(s.email, s.pass)
            if (user != null) {
                _currentUserId.value = user.id
                _loginState.value = LoginUiState() // limpia email/clave al entrar
                onSuccess()
            } else {
                _loginState.update {
                    it.copy(
                        isSubmitting = false,
                        authError = "Correo o contraseña incorrectos",
                        canSubmit = canLogin(it.email, it.pass, it.emailError)
                    )
                }
            }
        }
    }

    // ---------- REGISTRO ----------
    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    /** Recalcula TODOS los errores a partir de los textos y decide si el botón se habilita. */
    private fun revalidate(s: RegisterUiState): RegisterUiState {
        val nameErr = Validators.nameError(s.name)
        val emailErr = Validators.emailError(s.email, requireCorporate = true)
        val passErr = Validators.passwordError(s.pass)
        val confirmErr = Validators.confirmError(s.pass, s.confirm)
        val allFilled = s.name.isNotBlank() && s.email.isNotBlank() &&
            s.pass.isNotBlank() && s.confirm.isNotBlank()
        val noErrors = nameErr == null && emailErr == null && passErr == null && confirmErr == null
        return s.copy(
            nameError = nameErr,
            emailError = emailErr,
            passError = passErr,
            confirmError = confirmErr,
            strength = Validators.passwordStrength(s.pass),
            canSubmit = allFilled && noErrors && !s.isSubmitting
        )
    }

    fun onRegisterNameChange(v: String) =
        _registerState.update { revalidate(it.copy(name = v, registerError = null)) }

    fun onRegisterEmailChange(v: String) =
        _registerState.update { revalidate(it.copy(email = v, registerError = null)) }

    fun onRegisterPassChange(v: String) =
        _registerState.update { revalidate(it.copy(pass = v, registerError = null)) }

    fun onRegisterConfirmChange(v: String) =
        _registerState.update { revalidate(it.copy(confirm = v, registerError = null)) }

    fun clearRegisterForm() {
        _registerState.value = RegisterUiState()
    }

    fun register(onSuccess: () -> Unit) {
        val s = _registerState.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _registerState.update { it.copy(isSubmitting = true, canSubmit = false, registerError = null) }
            delay(600)
            repo.register(s.name, s.email, s.pass)
                .onSuccess { newId ->
                    _currentUserId.value = newId // la cuenta nueva queda con sesión iniciada
                    _registerState.value = RegisterUiState()
                    onSuccess()
                }
                .onFailure { error ->
                    _registerState.update {
                        revalidate(
                            it.copy(
                                isSubmitting = false,
                                registerError = error.message ?: "No se pudo crear la cuenta"
                            )
                        )
                    }
                }
        }
    }

    // ---------- SESIÓN ----------
    private val _currentUserId = MutableStateFlow<Int?>(null)

    /** Usuario logueado; se actualiza solo cuando Room cambia (ej: nueva foto de perfil). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf<UserEntity?>(null) else repo.observeUser(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() {
        _currentUserId.value = null
    }

    /** UPDATE de la foto: guarda la nueva ruta en Room y borra el archivo de la foto anterior. */
    fun setProfilePhoto(newPath: String?) {
        val id = _currentUserId.value ?: return
        viewModelScope.launch {
            val oldPath = repo.getPhotoPath(id)
            repo.updatePhoto(id, newPath)
            if (oldPath != null && oldPath != newPath) ImageUtils.deleteFile(oldPath)
        }
    }
}
