package com.example.retailflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.retailflow.data.repository.FlowRepository
import com.example.retailflow.data.repository.UserRepository

/**
 * Fábrica manual de ViewModels (inyección de dependencias a mano).
 * Necesaria porque nuestros ViewModels reciben un Repository en el constructor
 * y Android, por defecto, solo sabe crear ViewModels sin parámetros.
 */
class AppViewModelFactory(
    private val flowRepository: FlowRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(flowRepository) as T
        modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(userRepository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
