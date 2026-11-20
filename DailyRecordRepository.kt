package br.com.projeto180dias.data.repository

import br.com.projeto180dias.data.local.dao.DailyRecordDao
import br.com.projeto180dias.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

class DailyRecordRepository(private val dao: DailyRecordDao) {
    fun observeAll(): Flow<List<DailyRecordEntity>> = dao.observeAll()
    fun observeByDate(dateIso: String): Flow<DailyRecordEntity?> = dao.observeByDate(dateIso)
    suspend fun save(record: DailyRecordEntity) = dao.upsert(
        record.copy(updatedAtEpochMillis = System.currentTimeMillis())
    )
    suspend fun delete(dateIso: String) = dao.deleteByDate(dateIso)
}
