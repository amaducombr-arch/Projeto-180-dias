package br.com.projeto180dias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.projeto180dias.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueAtEpochMillis ASC, createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueAtEpochMillis ASC")
    fun observePending(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dayIso = :dayIso ORDER BY CASE priority WHEN 'HIGH' THEN 0 WHEN 'NORMAL' THEN 1 ELSE 2 END, scheduledAtEpochMillis ASC, createdAtEpochMillis ASC")
    fun observeForDay(dayIso: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(tasks: List<TaskEntity>): List<Long>

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed, status = :status, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun setCompletion(id: Long, completed: Boolean, status: String, updatedAt: Long)
}
