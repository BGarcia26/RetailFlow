package com.example.retailflow.domain

import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryEntity
import com.example.retailflow.util.FlowCalculations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Lógica de negocio del "Cerrar turno": convierte los registros individuales en vivo
 * en un único registro de historial. Vive fuera del ViewModel para poder probarla sola.
 */
object ShiftSummarizer {

    fun summarize(entries: List<FlowEntryEntity>, now: Date = Date()): HistoryEntity? {
        if (entries.isEmpty()) return null

        val totalPeople = entries.sumOf { it.groupSize }
        val buyersPeople = entries.filter { it.madePurchase }.sumOf { it.groupSize }
        val transitPeople = totalPeople - buyersPeople

        // Se cuentan REGISTROS (no personas) por tipo de visita
        val solo = entries.count { it.companionType == FlowCalculations.SOLO }
        val couple = entries.count { it.companionType == FlowCalculations.PAREJA }
        val group = entries.count { it.companionType == FlowCalculations.GRUPO }

        val summaryByAccess = entries
            .groupBy { it.accessName.substringBefore(" -") }
            .entries
            .joinToString(" | ") { "${it.key}: ${it.value.sumOf { e -> e.groupSize }} pers." }

        val dateFormatted = SimpleDateFormat("dd/MM/yyyy", FlowCalculations.APP_LOCALE).format(now)
        val timeFormatted = SimpleDateFormat("HH:mm", FlowCalculations.APP_LOCALE).format(now)
        val hour = Calendar.getInstance().apply { time = now }.get(Calendar.HOUR_OF_DAY)

        return HistoryEntity(
            dateLabel = "$dateFormatted (Hoy)",
            shiftTime = "${FlowCalculations.shiftSlotFor(hour)} ($timeFormatted hrs)",
            totalFlow = totalPeople,
            buyersCount = buyersPeople,
            transitOnlyPercentage = FlowCalculations.percent(transitPeople, totalPeople),
            childCount = entries.sumOf { it.childCount },
            youngCount = entries.sumOf { it.youngCount },
            adultCount = entries.sumOf { it.adultCount },
            seniorCount = entries.sumOf { it.seniorCount },
            soloCount = solo,
            coupleCount = couple,
            groupCount = group,
            accessSummary = summaryByAccess.ifEmpty { "Sin desglose" }
        )
    }
}
