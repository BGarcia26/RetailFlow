package com.example.retailflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryEntity
import com.example.retailflow.data.repository.FlowRepository
import com.example.retailflow.domain.ShiftSummarizer
import com.example.retailflow.util.FlowCalculations
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class HistoryViewModel(private val repo: FlowRepository) : ViewModel() {

    val historyList: StateFlow<List<HistoryEntity>> = repo.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entriesList: StateFlow<List<FlowEntryEntity>> = repo.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds

    private val _isSamplingActive = MutableStateFlow(false)
    val isSamplingActive: StateFlow<Boolean> = _isSamplingActive

    private var timerJob: Job? = null

    init {
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    fun toggleSamplingTimer() {
        if (_isSamplingActive.value) {
            _isSamplingActive.value = false
            timerJob?.cancel()
        } else {
            _isSamplingActive.value = true
            timerJob = viewModelScope.launch {
                while (_isSamplingActive.value) {
                    delay(1000L)
                    _timerSeconds.value += 1
                }
            }
        }
    }

    // ---------- CREATE ----------
    fun registerGroupFlow(
        accessName: String,
        direction: String,
        childCount: Int,
        youngCount: Int,
        adultCount: Int,
        seniorCount: Int,
        madePurchase: Boolean
    ) {
        val totalGroupSize = childCount + youngCount + adultCount + seniorCount
        if (totalGroupSize <= 0) return

        viewModelScope.launch {
            val currentTime = SimpleDateFormat("HH:mm:ss", FlowCalculations.APP_LOCALE).format(Date())
            repo.addEntry(
                FlowEntryEntity(
                    accessName = accessName,
                    direction = direction,
                    ageGroup = FlowCalculations.ageSummary(childCount, youngCount, adultCount, seniorCount),
                    companionType = FlowCalculations.companionTypeFor(totalGroupSize),
                    groupSize = totalGroupSize,
                    childCount = childCount,
                    youngCount = youngCount,
                    adultCount = adultCount,
                    seniorCount = seniorCount,
                    madePurchase = madePurchase,
                    timestamp = currentTime
                )
            )
        }
    }

    // ---------- UPDATE ----------
    /**
     * Edita un registro existente. Recalcula los campos derivados (resumen de edades,
     * tipo de visita y tamaño del grupo) para que nunca queden inconsistentes.
     * Conserva el id y la hora original: Room usa el id para saber QUÉ fila actualizar.
     */
    fun updateEntry(
        original: FlowEntryEntity,
        accessName: String,
        direction: String,
        childCount: Int,
        youngCount: Int,
        adultCount: Int,
        seniorCount: Int,
        madePurchase: Boolean
    ) {
        val total = childCount + youngCount + adultCount + seniorCount
        if (total <= 0) return
        viewModelScope.launch {
            repo.updateEntry(
                original.copy(
                    accessName = accessName,
                    direction = direction,
                    ageGroup = FlowCalculations.ageSummary(childCount, youngCount, adultCount, seniorCount),
                    companionType = FlowCalculations.companionTypeFor(total),
                    groupSize = total,
                    childCount = childCount,
                    youngCount = youngCount,
                    adultCount = adultCount,
                    seniorCount = seniorCount,
                    madePurchase = madePurchase
                )
            )
        }
    }

    // ---------- DELETE (+ deshacer) ----------
    fun deleteEntry(entry: FlowEntryEntity) {
        viewModelScope.launch { repo.deleteEntry(entry) }
    }

    /** Deshacer el borrado: se vuelve a insertar con el MISMO id (la estrategia REPLACE lo permite). */
    fun restoreEntry(entry: FlowEntryEntity) {
        viewModelScope.launch { repo.addEntry(entry) }
    }

    // ---------- CERRAR TURNO ----------
    fun closeShiftAndSaveToHistory() {
        viewModelScope.launch {
            val record = ShiftSummarizer.summarize(entriesList.value) ?: return@launch
            repo.closeShift(record)
            _timerSeconds.value = 0
            _isSamplingActive.value = false
            timerJob?.cancel()
        }
    }
}
