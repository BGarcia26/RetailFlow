package com.example.retailflow

import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.domain.ShiftSummarizer
import com.example.retailflow.util.ShiftReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ShiftSummarizerTest {

    private fun entry(access: String, adults: Int, seniors: Int, companion: String, buyer: Boolean) =
        FlowEntryEntity(
            accessName = access, direction = "Entrada", ageGroup = "x", companionType = companion,
            groupSize = adults + seniors, adultCount = adults, seniorCount = seniors,
            madePurchase = buyer, timestamp = "10:00:00"
        )

    @Test fun emptyShift_returnsNull() {
        assertNull(ShiftSummarizer.summarize(emptyList()))
    }

    @Test fun summarize_computesTotalsPercentAndGroups() {
        val entries = listOf(
            entry("Acceso 1 - Conexión Mall", 1, 0, "Solo", buyer = true),
            entry("Acceso 1 - Conexión Mall", 2, 0, "Pareja", buyer = false),
            entry("Acceso 2 - Estacionamiento", 2, 1, "Grupo/Familia", buyer = false)
        )
        val now = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 5, 16, 30, 0) }.time
        val r = ShiftSummarizer.summarize(entries, now)!!

        assertEquals(6, r.totalFlow)
        assertEquals(1, r.buyersCount)
        assertEquals(83, r.transitOnlyPercentage)       // 5 de 6 = 83.3%
        assertEquals(5, r.adultCount)
        assertEquals(1, r.seniorCount)
        assertEquals(1, r.soloCount)
        assertEquals(1, r.coupleCount)
        assertEquals(1, r.groupCount)
        assertEquals("05/11/2026 (Jueves)", r.dateLabel)
        assertTrue(r.shiftTime.startsWith("Tarde"))
        assertEquals("Acceso 1: 3 pers. | Acceso 2: 3 pers.", r.accessSummary)
    }

    @Test fun report_containsKeyFigures() {
        val entries = listOf(entry("Acceso 1 - Conexión Mall", 2, 0, "Pareja", buyer = true))
        val record = ShiftSummarizer.summarize(entries)!!
        val text = ShiftReport.build(record)
        assertTrue(text.contains("Flujo total: 2 personas"))
        assertTrue(text.contains("Compradores: 2 (100%)"))
    }
}
