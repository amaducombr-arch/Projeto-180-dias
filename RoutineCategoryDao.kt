package br.com.projeto180dias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.projeto180dias.data.local.entity.RoutineCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineCategoryDao {
    @Query("SELECT * FROM routine_categories ORDER BY isCustom ASC, name ASC")
    fun observeAll(): Flow<List<RoutineCategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: RoutineCategoryEntity): Long
    @Delete
    suspend fun delete(category: RoutineCategoryEntity)
}
