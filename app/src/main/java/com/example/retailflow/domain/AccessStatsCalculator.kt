package com.example.retailflow.domain

import com.example.retailflow.data.AccessZones
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryEntity
import com.example.retailflow.data.local.ShiftAccessEntity

/**
 * Calcula las estadísticas por acceso de un turno. Lógica pura (sin Android) y con tests.
 * El shiftId queda en 0 y lo completa el repositorio cuando ya conoce el id del turno guardado.
 */
object AccessStatsCalculator {

    private val ACCESS_PAIR = Regex("""(Acceso\s+\d+)\s*:\s*(\d+)""")

    /** EXACTO: se calcula con los registros reales del turno que se está cerrando. */
    fun fromEntries(entries: List<FlowEntryEntity>): List<ShiftAccessEntity> =
        entries
            .groupBy { it.accessName }
            .map { (accessName, list) ->
                ShiftAccessEntity(
                    shiftId = 0,
                    accessName = accessName,
                    totalPeople = list.sumOf { it.groupSize },
                    buyersCount = list.filter { it.madePurchase }.sumOf { it.groupSize }
                )
            }

    /**
     * ESTIMADO: solo para turnos que ya estaban guardados antes de existir esta tabla
     * (los cerrados con versiones anteriores de la app). De ellos solo se sabe
     * el total por acceso (texto "Acceso 1: 3100 | ...") y el total de compradores del turno,
     * así que los compradores se reparten proporcionalmente al flujo de cada acceso.
     * Se usa el método del "mayor resto" para que la suma sea EXACTAMENTE buyersCount.
     */
    fun estimateFromSummary(shift: HistoryEntity): List<ShiftAccessEntity> {
        val totals = ACCESS_PAIR.findAll(shift.accessSummary).mapNotNull { match ->
            val zone = AccessZones.all.firstOrNull { it.shortName.equals(match.groupValues[1], ignoreCase = true) }
            val people = match.groupValues[2].toIntOrNull()
            if (zone != null && people != null) zone.name to people else null
        }.toList()

        val sumTotals = totals.sumOf { it.second }
        if (sumTotals <= 0) return emptyList()

        val target = shift.buyersCount.coerceIn(0, sumTotals)
        val buyers = totals.map { (it.second.toLong() * target / sumTotals).toInt() }.toMutableList()
        val byRemainder = totals.indices.sortedByDescending { totals[it].second.toLong() * target % sumTotals }
        var missing = target - buyers.sum()
        for (i in byRemainder) {
            if (missing <= 0) break
            buyers[i] += 1
            missing--
        }

        return totals.mapIndexed { i, (accessName, people) ->
            ShiftAccessEntity(shiftId = 0, accessName = accessName, totalPeople = people, buyersCount = buyers[i])
        }
    }
}
