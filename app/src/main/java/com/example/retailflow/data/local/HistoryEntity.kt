package com.example.retailflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flow_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateLabel: String,
    val shiftTime: String,
    val totalFlow: Int,
    val buyersCount: Int,
    val transitOnlyPercentage: Int,
    val childCount: Int = 0,
    val youngCount: Int = 0,
    val adultCount: Int = 0,
    val seniorCount: Int = 0,
    val soloCount: Int = 0,
    val coupleCount: Int = 0,
    val groupCount: Int = 0,
    val accessSummary: String = "Sin desglose por acceso"
)