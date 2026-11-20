package br.com.projeto180dias.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.projeto180dias.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records ORDER BY dateIso DESC")
    fun observeAll(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE dateIso = :dateIso LIMIT 1")
    fun observeByDate(dateIso: String): Flow<DailyRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DailyRecordEntity): Long

    @Update
    suspend fun update(record: DailyRecordEntity)

    @Query("DELETE FROM daily_records WHERE dateIso = :dateIso")
    suspend fun deleteByDate(dateIso: String)
}
