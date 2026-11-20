package br.com.projeto180dias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index("dueAtEpochMillis"), Index("status"), Index("dayIso"), Index("recurrenceGroupId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueAtEpochMillis: Long? = null,
    val isCompleted: Boolean = false,
    val priority: String = "NORMAL",
    val status: String = "TODO",
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
    val dayIso: String? = null,
    @ColumnInfo(defaultValue = "'Autocuidado'") val category: String = "Autocuidado",
    @ColumnInfo(defaultValue = "'📝'") val emoji: String = "📝",
    @ColumnInfo(defaultValue = "''") val notes: String = "",
    val scheduledAtEpochMillis: Long? = null,
    @ColumnInfo(defaultValue = "30") val durationMinutes: Int = 30,
    @ColumnInfo(defaultValue = "'NONE'") val recurrenceRule: String = "NONE",
    val recurrenceGroupId: String? = null
)
