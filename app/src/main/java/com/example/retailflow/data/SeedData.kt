package com.example.retailflow.data

import com.example.retailflow.data.local.HistoryEntity

/** Turnos de ejemplo que se insertan la primera vez para que el historial no parta vacío. */
object SeedData {
    fun sampleHistory(): List<HistoryEntity> = listOf(
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
}
