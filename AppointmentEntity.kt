package br.com.projeto180dias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Compromisso com horários armazenados como epoch millis para evitar ambiguidades de fuso. */
@Entity(
    tableName = "appointments",
    indices = [Index("startsAtEpochMillis"), Index("endsAtEpochMillis")]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val location: String = "",
    val startsAtEpochMillis: Long,
    val endsAtEpochMillis: Long? = null,
    @ColumnInfo(defaultValue = "0") val isCompleted: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
