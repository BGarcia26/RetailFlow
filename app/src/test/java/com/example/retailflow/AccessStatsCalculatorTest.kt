package com.example.retailflow

import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryEntity
import com.example.retailflow.domain.AccessStatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessStatsCalculatorTest {

    private val a1 = "Acceso 1 - Conexión Mall"
    private val a2 = "Acceso 2 - Estacionamiento"

    private fun entry(access: String, size: Int, buyer: Boolean) = FlowEntryEntity(
        accessName = access, direction = "Entrada", ageGroup = "x", companionType = "Solo",
        groupSize = size, adultCount = size, madePurchase = buyer, timestamp = "10:00:00"
    )

    private fun shift(buyers: Int, summary: String, total: Int = 0) = HistoryEntity(
        dateLabel = "x", shiftTime = "y", totalFlow = total, buyersCount = buyers,
        transitOnlyPercentage = 0, accessSummary = summary
    )

    @Test fun fromEntries_sumsTotalsAndBuyersPerAccess() {
        val stats = AccessStatsCalculator.fromEntries(
            listOf(entry(a1, 2, true), entry(a1, 1, false), entry(a2, 3, true))
        )
        assertEquals(2, stats.size)
        val s1 = stats.first { it.accessName == a1 }
        assertEquals(3, s1.totalPeople)
        assertEquals(2, s1.buyersCount)
        val s2 = stats.first { it.accessName == a2 }
        assertEquals(3, s2.totalPeople)
        assertEquals(3, s2.buyersCount)
    }

    @Test fun fromEntries_emptyGivesEmpty() {
        assertTrue(AccessStatsCalculator.fromEntries(emptyList()).isEmpty())
    }

    @Test fun estimate_seedShiftKeepsExactTotals() {
        val stats = AccessStatsCalculator.estimateFromSummary(
            shift(3550, "Acceso 1: 3100 | Acceso 2: 2400 | Acceso 3: 1390 | Acceso 4: 1000", 7890)
        )
        assertEquals(4, stats.size)
        assertEquals(a1, stats[0].accessName)
        assertEquals(7890, stats.sumOf { it.totalPeople })
        assertEquals(3550, stats.sumOf { it.buyersCount })
        stats.forEach { assertTrue(it.buyersCount <= it.totalPeople) }
    }

    @Test fun estimate_understandsPersSuffixOfClosedShifts() {
        val stats = AccessStatsCalculator.estimateFromSummary(shift(1, "Acceso 1: 3 pers. | Acceso 2: 3 pers."))
        assertEquals(6, stats.sumOf { it.totalPeople })
        assertEquals(1, stats.sumOf { it.buyersCount })
    }

    @Test fun estimate_withoutBreakdownGivesEmpty() {
        assertTrue(AccessStatsCalculator.estimateFromSummary(shift(5, "Sin desglose")).isEmpty())
        assertTrue(AccessStatsCalculator.estimateFromSummary(shift(5, "Acceso 9: 5")).isEmpty())
    }
}
