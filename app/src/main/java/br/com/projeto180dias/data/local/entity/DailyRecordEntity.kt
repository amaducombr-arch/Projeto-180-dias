package br.com.projeto180dias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records", indices = [Index(value = ["dateIso"], unique = true)])
data class DailyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String,
    val notes: String = "",
    val mood: String? = null,
    val energyLevel: Int? = null,
    val completedTasksCount: Int = 0,
    @ColumnInfo(defaultValue = "''") val goal: String = "",
    @ColumnInfo(defaultValue = "''") val evaluation: String = "",
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
