package com.example.retailflow.data.repository

import androidx.room.withTransaction
import com.example.retailflow.data.local.AccessTotals
import com.example.retailflow.data.local.AppDatabase
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryEntity
import com.example.retailflow.data.local.ShiftAccessEntity
import com.example.retailflow.domain.AccessStatsCalculator
import kotlinx.coroutines.flow.Flow

/**
 * Única puerta de acceso a los datos de flujo/historial.
 * El ViewModel ya no conoce el DAO: si mañana los datos vinieran de una API,
 * solo cambia este archivo.
 */
class FlowRepository(private val db: AppDatabase) {
    private val dao = db.historyDao()

    val history: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val entries: Flow<List<FlowEntryEntity>> = dao.getAllEntries()

    /** Personas y compradores por acceso, sumando TODOS los turnos cerrados. */
    val accessTotals: Flow<List<AccessTotals>> = dao.getAccessTotals()

    suspend fun repairDateLabels() = dao.repairDateLabels()

    suspend fun addEntry(entry: FlowEntryEntity) = dao.insertEntry(entry)

    suspend fun updateEntry(entry: FlowEntryEntity) = dao.updateEntry(entry)

    suspend fun deleteEntry(entry: FlowEntryEntity) = dao.deleteEntry(entry)

    /**
     * Genera las estadísticas por acceso de los turnos que todavía no las tienen
     * (turnos guardados antes de que existiera la tabla).
     * Para esos turnos los compradores por acceso son una ESTIMACIÓN proporcional;
     * los turnos que se cierren desde ahora guardan el valor exacto.
     */
    suspend fun backfillAccessStats() {
        db.withTransaction {
            val stats = dao.getShiftsWithoutStats().flatMap { shift ->
                AccessStatsCalculator.estimateFromSummary(shift).map { it.copy(shiftId = shift.id) }
            }
            if (stats.isNotEmpty()) dao.insertAccessStats(stats)
        }
    }

    /**
     * Guarda el turno, sus estadísticas por acceso y limpia los registros en vivo
     * en UNA transacción (o se hace todo o nada).
     */
    suspend fun closeShift(record: HistoryEntity, accessStats: List<ShiftAccessEntity>) {
        db.withTransaction {
            val shiftId = dao.insertRecord(record).toInt()
            dao.insertAccessStats(accessStats.map { it.copy(shiftId = shiftId) })
            dao.clearAllEntries()
        }
    }
}
