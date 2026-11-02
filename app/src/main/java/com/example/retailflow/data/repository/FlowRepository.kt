package com.example.retailflow.data.repository

import androidx.room.withTransaction
import com.example.retailflow.data.SeedData
import com.example.retailflow.data.local.AppDatabase
import com.example.retailflow.data.local.FlowEntryEntity
import com.example.retailflow.data.local.HistoryEntity
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

    suspend fun seedIfEmpty() {
        if (dao.getCount() == 0) dao.insertAll(SeedData.sampleHistory())
    }

    suspend fun addEntry(entry: FlowEntryEntity) = dao.insertEntry(entry)

    suspend fun updateEntry(entry: FlowEntryEntity) = dao.updateEntry(entry)

    suspend fun deleteEntry(entry: FlowEntryEntity) = dao.deleteEntry(entry)

    /** Guarda el turno y limpia los registros en vivo en UNA transacción (o se hace todo o nada). */
    suspend fun closeShift(record: HistoryEntity) {
        db.withTransaction {
            dao.insertRecord(record)
            dao.clearAllEntries()
        }
    }
}
