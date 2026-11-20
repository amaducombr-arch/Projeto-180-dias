package br.com.projeto180dias.data.repository

import br.com.projeto180dias.data.local.dao.TaskDao
import br.com.projeto180dias.data.local.entity.TaskEntity
import br.com.projeto180dias.domain.RoutineDateRules
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class TaskRepository(private val dao: TaskDao) {
    fun observeAll(): Flow<List<TaskEntity>> = dao.observeAll()
    fun observePending(): Flow<List<TaskEntity>> = dao.observePending()
    fun observeById(id: Long): Flow<TaskEntity?> = dao.observeById(id)
    fun observeForDay(dateIso: String): Flow<List<TaskEntity>> = dao.observeForDay(dateIso)

    suspend fun create(task: TaskEntity): Long = dao.insert(task)

    /** Cria ocorrências reais e independentes até o último dia do ciclo de 180 dias. */
    suspend fun createWithRecurrence(task: TaskEntity, endDate: LocalDate): List<Long> {
        val startDate = task.dayIso?.let { LocalDate.parse(it) } ?: return listOf(dao.insert(task))
        val rule = task.recurrenceRule
        if (rule == "NONE") return listOf(dao.insert(task))
        val groupId = UUID.randomUUID().toString()
        val occurrences = RoutineDateRules.recurrenceDates(startDate, endDate, rule).map { date ->
            val scheduled = task.scheduledAtEpochMillis?.let { base ->
                val localTime = java.time.Instant.ofEpochMilli(base).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                date.atTime(localTime).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            task.copy(id = 0, dayIso = date.toString(), dueAtEpochMillis = scheduled, scheduledAtEpochMillis = scheduled, recurrenceGroupId = groupId)
        }
        return if (occurrences.isEmpty()) listOf(dao.insert(task)) else dao.insertAll(occurrences)
    }

    suspend fun update(task: TaskEntity) = dao.update(task.copy(updatedAtEpochMillis = System.currentTimeMillis()))
    suspend fun delete(task: TaskEntity) = dao.delete(task)
    suspend fun duplicate(task: TaskEntity, dateIso: String = task.dayIso ?: LocalDate.now().toString()): Long =
        dao.insert(task.copy(id = 0, dayIso = dateIso, isCompleted = false, status = "TODO", recurrenceRule = "NONE", recurrenceGroupId = null, createdAtEpochMillis = System.currentTimeMillis(), updatedAtEpochMillis = System.currentTimeMillis()))

    suspend fun setCompleted(id: Long, completed: Boolean) = dao.setCompletion(
        id = id,
        completed = completed,
        status = if (completed) "DONE" else "TODO",
        updatedAt = System.currentTimeMillis()
    )
}
