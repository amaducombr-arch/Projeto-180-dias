package br.com.projeto180dias.data.repository

import br.com.projeto180dias.data.local.dao.ChecklistTemplateDao
import br.com.projeto180dias.data.local.entity.ChecklistTemplateEntity
import kotlinx.coroutines.flow.Flow

class ChecklistTemplateRepository(private val dao: ChecklistTemplateDao) {
    fun observeAll(): Flow<List<ChecklistTemplateEntity>> = dao.observeAll()
    suspend fun create(name: String, category: String, titles: List<String>) {
        val clean = titles.map(String::trim).filter(String::isNotEmpty)
        if (name.isNotBlank() && clean.isNotEmpty()) dao.insert(ChecklistTemplateEntity(name = name.trim(), category = category, taskTitles = clean.joinToString("\n")))
    }
    suspend fun delete(template: ChecklistTemplateEntity) = dao.delete(template)
}
