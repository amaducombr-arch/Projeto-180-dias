package br.com.projeto180dias.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.projeto180dias.data.local.dao.AppointmentDao
import br.com.projeto180dias.data.local.dao.ChecklistTemplateDao
import br.com.projeto180dias.data.local.dao.DailyRecordDao
import br.com.projeto180dias.data.local.dao.RoutineCategoryDao
import br.com.projeto180dias.data.local.dao.TaskDao
import br.com.projeto180dias.data.local.entity.AppointmentEntity
import br.com.projeto180dias.data.local.entity.ChecklistTemplateEntity
import br.com.projeto180dias.data.local.entity.DailyRecordEntity
import br.com.projeto180dias.data.local.entity.RoutineCategoryEntity
import br.com.projeto180dias.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, AppointmentEntity::class, DailyRecordEntity::class, RoutineCategoryEntity::class, ChecklistTemplateEntity::class],
    version = 3,
    exportSchema = true
)
abstract class ProjetoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun routineCategoryDao(): RoutineCategoryDao
    abstract fun checklistTemplateDao(): ChecklistTemplateDao
}
