package com.example.retailflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryDao
import com.example.retailflow.data.local.HistoryEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryViewModel(private val dao: HistoryDao) : ViewModel() {

    val historyList: StateFlow<List<HistoryEntity>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entriesList: StateFlow<List<FlowEntryEntity>> = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds

    private val _isSamplingActive = MutableStateFlow(false)
    val isSamplingActive: StateFlow<Boolean> = _isSamplingActive

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            if (dao.getCount() == 0) {
                dao.insertAll(
                    listOf(
                        HistoryEntity(
                            dateLabel = "13/08/2026 (Martes)",
                            shiftTime = "Mañana (09:00 - 14:00)",
                            totalFlow = 7890,
                            buyersCount = 3550,
                            transitOnlyPercentage = 55,
                            childCount = 600,
                            youngCount = 1800,
                            adultCount = 3990,
                            seniorCount = 1500,
                            soloCount = 3100,
                            coupleCount = 1400,
                            groupCount = 650,
                            accessSummary = "Acceso 1: 3100 | Acceso 2: 2400 | Acceso 3: 1390 | Acceso 4: 1000"
                        ),
                        HistoryEntity(
                            dateLabel = "14/08/2026 (Miércoles)",
                            shiftTime = "Tarde (14:00 - 20:00)",
                            totalFlow = 8850,
                            buyersCount = 3717,
                            transitOnlyPercentage = 58,
                            childCount = 850,
                            youngCount = 2400,
                            adultCount = 4200,
                            seniorCount = 1400,
                            soloCount = 2900,
                            coupleCount = 1800,
                            groupCount = 780,
                            accessSummary = "Acceso 1: 3600 | Acceso 2: 2650 | Acceso 3: 1600 | Acceso 4: 1000"
                        ),
                        HistoryEntity(
                            dateLabel = "15/08/2026 (Ayer)",
                            shiftTime = "Noche (20:00 - 22:30)",
                            totalFlow = 11400,
                            buyersCount = 3192,
                            transitOnlyPercentage = 72,
                            childCount = 1100,
                            youngCount = 3600,
                            adultCount = 5200,
                            seniorCount = 1500,
                            soloCount = 4200,
                            coupleCount = 2100,
                            groupCount = 950,
                            accessSummary = "Acceso 1: 4900 | Acceso 2: 3100 | Acceso 3: 2000 | Acceso 4: 1400"
                        )
                    )
                )
            }
        }
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

        val companionType = when (totalGroupSize) {
            1 -> "Solo"
            2 -> "Pareja"
            else -> "Grupo/Familia"
        }

        val parts = mutableListOf<String>()
        if (seniorCount > 0) parts.add("$seniorCount Senior")
        if (adultCount > 0) parts.add("$adultCount Adulto")
        if (youngCount > 0) parts.add("$youngCount Joven")
        if (childCount > 0) parts.add("$childCount Niño")
        val ageSummary = parts.joinToString(", ")

        viewModelScope.launch {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            dao.insertEntry(
                FlowEntryEntity(
                    accessName = accessName,
                    direction = direction,
                    ageGroup = ageSummary,
                    companionType = companionType,
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

    fun deleteEntry(entry: FlowEntryEntity) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }

    fun closeShiftAndSaveToHistory() {
        viewModelScope.launch {
            val currentEntries = entriesList.value
            if (currentEntries.isNotEmpty()) {
                val totalPeople = currentEntries.sumOf { it.groupSize }
                val buyersPeople = currentEntries.filter { it.madePurchase }.sumOf { it.groupSize }
                val transitPeople = totalPeople - buyersPeople
                val leakPercent = if (totalPeople > 0) (transitPeople * 100) / totalPeople else 0

                val children = currentEntries.sumOf { it.childCount }
                val young = currentEntries.sumOf { it.youngCount }
                val adult = currentEntries.sumOf { it.adultCount }
                val senior = currentEntries.sumOf { it.seniorCount }

                val solo = currentEntries.count { it.companionType == "Solo" }
                val couple = currentEntries.count { it.companionType == "Pareja" }
                val group = currentEntries.count { it.companionType == "Grupo/Familia" }

                val summaryByAccess = currentEntries
                    .groupBy { it.accessName.substringBefore(" -") }
                    .entries
                    .joinToString(" | ") { "${it.key}: ${it.value.sumOf { e -> e.groupSize }} pers." }

                val now = Date()
                val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val shiftSlot = when {
                    hour < 14 -> "Mañana"
                    hour < 20 -> "Tarde"
                    else -> "Noche"
                }

                dao.insertRecord(
                    HistoryEntity(
                        dateLabel = "$dateFormatted (Hoy)",
                        shiftTime = "$shiftSlot ($timeFormatted hrs)",
                        totalFlow = totalPeople,
                        buyersCount = buyersPeople,
                        transitOnlyPercentage = leakPercent,
                        childCount = children,
                        youngCount = young,
                        adultCount = adult,
                        seniorCount = senior,
                        soloCount = solo,
                        coupleCount = couple,
                        groupCount = group,
                        accessSummary = summaryByAccess.ifEmpty { "Sin desglose" }
                    )
                )
                dao.clearAllEntries()
                _timerSeconds.value = 0
                _isSamplingActive.value = false
                timerJob?.cancel()
            }
        }
    }
}

class HistoryViewModelFactory(private val dao: HistoryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}