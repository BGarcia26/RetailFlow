package com.example.retailflow

import com.example.retailflow.util.FlowCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class FlowCalculationsTest {

    @Test fun companionType_bySize() {
        assertEquals("Solo", FlowCalculations.companionTypeFor(1))
        assertEquals("Pareja", FlowCalculations.companionTypeFor(2))
        assertEquals("Grupo/Familia", FlowCalculations.companionTypeFor(3))
        assertEquals("Grupo/Familia", FlowCalculations.companionTypeFor(7))
    }

    @Test fun ageSummary_ordersSeniorFirstAndSkipsZeros() {
        assertEquals("2 Senior, 1 Adulto, 1 Niño", FlowCalculations.ageSummary(child = 1, young = 0, adult = 1, senior = 2))
        assertEquals("", FlowCalculations.ageSummary(0, 0, 0, 0))
    }

    @Test fun percent_roundsInsteadOfTruncating() {
        assertEquals(67, FlowCalculations.percent(2, 3))   // 66.6 -> 67
        assertEquals(33, FlowCalculations.percent(1, 3))
        assertEquals(0, FlowCalculations.percent(5, 0))    // evita dividir por cero
        assertEquals(100, FlowCalculations.percent(8, 8))
    }

    @Test fun shiftSlot_byHour() {
        assertEquals("Mañana", FlowCalculations.shiftSlotFor(9))
        assertEquals("Tarde", FlowCalculations.shiftSlotFor(14))
        assertEquals("Tarde", FlowCalculations.shiftSlotFor(19))
        assertEquals("Noche", FlowCalculations.shiftSlotFor(20))
    }

    @Test fun timer_format() {
        assertEquals("00:05", FlowCalculations.formatTimer(5))
        assertEquals("02:03", FlowCalculations.formatTimer(123))
    }
}
