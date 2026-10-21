package com.example.retailflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flow_entries")
data class FlowEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accessName: String,
    val direction: String,
    val ageGroup: String,       // Resumen legible, ej: "2 Seniors, 1 Joven, 1 Niño"
    val companionType: String,  // Calculado: "Solo", "Pareja" o "Grupo/Familia"
    val groupSize: Int,         // Suma total del grupo
    val childCount: Int = 0,
    val youngCount: Int = 0,
    val adultCount: Int = 0,
    val seniorCount: Int = 0,
    val madePurchase: Boolean,
    val timestamp: String
)