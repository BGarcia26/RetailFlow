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

    /** "1 persona", "2 personas" y también "0 personas": en español el cero va en plural. */
    fun countLabel(count: Int, singular: String, plural: String): String =
        "$count ${if (count == 1) singular else plural}"

    fun ageSummary(child: Int, young: Int, adult: Int, senior: Int): String {
        val parts = mutableListOf<String>()
        if (senior > 0) parts.add(countLabel(senior, "Senior", "Seniors"))
        if (adult > 0) parts.add(countLabel(adult, "Adulto", "Adultos"))
        if (young > 0) parts.add(countLabel(young, "Joven", "Jóvenes"))
        if (child > 0) parts.add(countLabel(child, "Niño", "Niños"))
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
