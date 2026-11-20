package br.com.projeto180dias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import br.com.projeto180dias.data.local.entity.ChecklistTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistTemplateDao {
    @Query("SELECT * FROM checklist_templates ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ChecklistTemplateEntity>>
    @Insert
    suspend fun insert(template: ChecklistTemplateEntity): Long
    @Delete
    suspend fun delete(template: ChecklistTemplateEntity)
}
