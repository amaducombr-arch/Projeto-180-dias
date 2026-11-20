package br.com.projeto180dias.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "routine_categories", indices = [Index(value = ["name"], unique = true)])
data class RoutineCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📌",
    val isCustom: Boolean = true
)
