package br.com.projeto180dias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.projeto180dias.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY startsAtEpochMillis ASC")
    fun observeAll(): Flow<List<AppointmentEntity>>

    /** Intervalos semiabertos: [início, fim). Inclui eventos que começaram antes e ainda estão ativos. */
    @Query("SELECT * FROM appointments WHERE startsAtEpochMillis < :until AND COALESCE(endsAtEpochMillis, startsAtEpochMillis + 1) > :from ORDER BY startsAtEpochMillis ASC")
    fun observeBetween(from: Long, until: Long): Flow<List<AppointmentEntity>>

    @Query("UPDATE appointments SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompletion(id: Long, completed: Boolean)

    @Query("SELECT COUNT(*) FROM appointments WHERE startsAtEpochMillis < :until AND COALESCE(endsAtEpochMillis, startsAtEpochMillis + 1) > :from")
    fun observeCountBetween(from: Long, until: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Delete
    suspend fun delete(appointment: AppointmentEntity)
}
