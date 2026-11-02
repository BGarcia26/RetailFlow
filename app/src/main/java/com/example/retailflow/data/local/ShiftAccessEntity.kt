package com.example.retailflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla `shift_access_stats`: cuántas personas pasaron (y cuántas compraron) por CADA acceso
 * en CADA turno cerrado. Es lo que permite que el Dashboard sume datos reales del historial
 * (antes el desglose solo existía como texto dentro de HistoryEntity.accessSummary).
 */
@Entity(tableName = "shift_access_stats")
data class ShiftAccessEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shiftId: Int,          // id del turno en flow_history
    val accessName: String,    // nombre completo, ej: "Acceso 1 - Conexión Mall"
    val totalPeople: Int,
    val buyersCount: Int
)

/** Resultado de la consulta que suma todos los turnos por acceso (no es una tabla). */
data class AccessTotals(
    val accessName: String,
    val totalPeople: Int,
    val buyersCount: Int
)
