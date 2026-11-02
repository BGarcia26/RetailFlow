package com.example.retailflow.util

import java.util.Locale

/** Cálculos puros del negocio (sin Android) para poder probarlos con tests unitarios. */
object FlowCalculations {
    const val SOLO = "Solo"
    const val PAREJA = "Pareja"
    const val GRUPO = "Grupo/Familia"

    /** Locale fijo para que las fechas/horas se formateen igual en cualquier teléfono. */
    val APP_LOCALE: Locale = Locale.forLanguageTag("es-CL")

    fun companionTypeFor(totalPeople: Int): String = when (totalPeople) {
        1 -> SOLO
        2 -> PAREJA
        else -> GRUPO
    }

    fun ageSummary(child: Int, young: Int, adult: Int, senior: Int): String {
        val parts = mutableListOf<String>()
        if (senior > 0) parts.add("$senior Senior")
        if (adult > 0) parts.add("$adult Adulto")
        if (young > 0) parts.add("$young Joven")
        if (child > 0) parts.add("$child Niño")
        return parts.joinToString(", ")
    }

    /** Porcentaje entero REDONDEADO (antes se truncaba: 66.6% mostraba 66%). */
    fun percent(part: Int, total: Int): Int =
        if (total <= 0) 0 else Math.round(part * 100f / total)

    fun shiftSlotFor(hourOfDay: Int): String = when {
        hourOfDay < 14 -> "Mañana"
        hourOfDay < 20 -> "Tarde"
        else -> "Noche"
    }

    fun formatTimer(totalSeconds: Int): String =
        String.format(APP_LOCALE, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
