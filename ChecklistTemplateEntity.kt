package br.com.projeto180dias.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_templates")
data class ChecklistTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Lista de títulos separados por quebra de linha; cada linha é convertida em uma tarefa ao aplicar. */
    val taskTitles: String,
    val category: String = "Autocuidado",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
