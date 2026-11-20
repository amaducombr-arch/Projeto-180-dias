package br.com.projeto180dias.data.repository

import br.com.projeto180dias.data.local.dao.AppointmentDao
import br.com.projeto180dias.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val dao: AppointmentDao) {
    fun observeAll(): Flow<List<AppointmentEntity>> = dao.observeAll()
    fun observeBetween(fromEpochMillis: Long, untilEpochMillis: Long): Flow<List<AppointmentEntity>> =
        dao.observeBetween(fromEpochMillis, untilEpochMillis)
    suspend fun create(appointment: AppointmentEntity): Long = dao.insert(appointment)
    suspend fun update(appointment: AppointmentEntity) = dao.update(appointment)
    suspend fun delete(appointment: AppointmentEntity) = dao.delete(appointment)
    suspend fun setCompleted(id: Long, completed: Boolean) = dao.setCompletion(id, completed)
}
