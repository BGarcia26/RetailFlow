package com.example.retailflow.util

import com.example.retailflow.data.local.HistoryEntity

/** Arma el texto que se comparte por WhatsApp/correo/etc. desde el detalle de un turno. */
object ShiftReport {
    fun subject(shift: HistoryEntity): String = "Reporte de turno - ${shift.dateLabel}"

    fun build(shift: HistoryEntity): String {
        val transitCount = shift.totalFlow - shift.buyersCount
        val buyerPct = 100 - shift.transitOnlyPercentage
        return buildString {
            appendLine("📊 Reporte de turno - Rincón Sur")
            appendLine("Fecha: ${shift.dateLabel}")
            appendLine("Horario: ${shift.shiftTime}")
            appendLine()
            appendLine("Flujo total: ${FlowCalculations.countLabel(shift.totalFlow, "persona", "personas")}")
            appendLine("🛍️ Compradores: ${shift.buyersCount} ($buyerPct%)")
            appendLine("🚶 Solo tránsito: $transitCount (${shift.transitOnlyPercentage}%)")
            appendLine()
            appendLine(
                "Edades: Niños ${shift.childCount} | Jóvenes ${shift.youngCount} | " +
                    "Adultos ${shift.adultCount} | Seniors ${shift.seniorCount}"
            )
            appendLine(
                "Visitas: Solos ${shift.soloCount} | Parejas ${shift.coupleCount} | Grupos ${shift.groupCount}"
            )
            appendLine("Accesos: ${shift.accessSummary}")
            appendLine()
            append("Generado con RetailFlow")
        }
    }
}
