package br.com.projeto180dias.data.repository

import br.com.projeto180dias.data.local.dao.RoutineCategoryDao
import br.com.projeto180dias.data.local.entity.RoutineCategoryEntity
import kotlinx.coroutines.flow.Flow

class RoutineCategoryRepository(private val dao: RoutineCategoryDao) {
    fun observeAll(): Flow<List<RoutineCategoryEntity>> = dao.observeAll()
    suspend fun create(name: String, emoji: String) {
        val cleanName = name.trim()
        if (cleanName.isNotEmpty()) dao.insert(RoutineCategoryEntity(name = cleanName, emoji = emoji.ifBlank { "📌" }))
    }
    suspend fun delete(category: RoutineCategoryEntity) = dao.delete(category)
}
