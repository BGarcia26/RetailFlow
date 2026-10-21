package com.example.retailflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    // --- HISTORIAL DE DÍAS CERRADOS ---
    @Query("SELECT * FROM flow_history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HistoryEntity)

    @Query("SELECT COUNT(*) FROM flow_history")
    suspend fun getCount(): Int

    // --- REGISTROS DEL FORMULARIO EN VIVO ---
    @Query("SELECT * FROM flow_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<FlowEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FlowEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FlowEntryEntity)

    @Query("DELETE FROM flow_entries")
    suspend fun clearAllEntries()
}