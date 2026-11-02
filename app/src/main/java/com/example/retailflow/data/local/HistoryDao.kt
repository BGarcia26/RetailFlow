package com.example.retailflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    // --- HISTORIAL DE DÍAS CERRADOS ---
    @Query("SELECT * FROM flow_history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HistoryEntity): Long // devuelve el id del turno recién guardado

    @Query("SELECT COUNT(*) FROM flow_history")
    suspend fun getCount(): Int

    // Recalcula el día de la semana desde la fecha ("%w" da 0 = domingo). Repara los turnos antiguos
    // que quedaron guardados como "(Hoy)", "(Ayer)" o con un día equivocado. Solo cambia datos, no el
    // esquema, por eso no necesita una migración. Las fechas inválidas no se tocan: darían NULL.
    @Query(
        "UPDATE flow_history SET dateLabel = substr(dateLabel, 1, 10) || ' (' || " +
            "CASE strftime('%w', substr(dateLabel, 7, 4) || '-' || substr(dateLabel, 4, 2) || '-' || substr(dateLabel, 1, 2)) " +
            "WHEN '0' THEN 'Domingo' WHEN '1' THEN 'Lunes' WHEN '2' THEN 'Martes' WHEN '3' THEN 'Miércoles' " +
            "WHEN '4' THEN 'Jueves' WHEN '5' THEN 'Viernes' ELSE 'Sábado' END || ')' " +
            "WHERE dateLabel GLOB '[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9] (*)' " +
            "AND strftime('%w', substr(dateLabel, 7, 4) || '-' || substr(dateLabel, 4, 2) || '-' || substr(dateLabel, 1, 2)) IS NOT NULL"
    )
    suspend fun repairDateLabels()

    // --- ESTADÍSTICAS POR ACCESO DE CADA TURNO CERRADO (alimentan el Dashboard) ---
    @Insert
    suspend fun insertAccessStats(stats: List<ShiftAccessEntity>)

    // Suma de TODOS los turnos cerrados, agrupada por acceso. Es un Flow: si se cierra un turno, el Dashboard se actualiza solo.
    @Query(
        "SELECT accessName, SUM(totalPeople) AS totalPeople, SUM(buyersCount) AS buyersCount " +
            "FROM shift_access_stats GROUP BY accessName"
    )
    fun getAccessTotals(): Flow<List<AccessTotals>>

    // Turnos que todavía no tienen estadísticas por acceso (de ejemplo o cerrados antes de esta versión)
    @Query("SELECT * FROM flow_history WHERE id NOT IN (SELECT shiftId FROM shift_access_stats)")
    suspend fun getShiftsWithoutStats(): List<HistoryEntity>

    // --- REGISTROS DEL FORMULARIO EN VIVO ---
    @Query("SELECT * FROM flow_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<FlowEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FlowEntryEntity)

    // UPDATE: edita un registro existente (se identifica por su clave primaria)
    @Update
    suspend fun updateEntry(entry: FlowEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FlowEntryEntity)

    @Query("DELETE FROM flow_entries")
    suspend fun clearAllEntries()
}