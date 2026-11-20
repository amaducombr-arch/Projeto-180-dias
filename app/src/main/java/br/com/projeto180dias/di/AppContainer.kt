package br.com.projeto180dias.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.projeto180dias.data.local.ProjetoDatabase
import br.com.projeto180dias.data.repository.RoutineCategoryRepository
import br.com.projeto180dias.data.repository.ChecklistTemplateRepository
import br.com.projeto180dias.data.preferences.SettingsRepository
import br.com.projeto180dias.data.repository.AppointmentRepository
import br.com.projeto180dias.data.repository.DailyRecordRepository
import br.com.projeto180dias.data.repository.TaskRepository

/** Composição manual de dependências; pode ser substituída por Hilt/Koin em etapa futura. */
class AppContainer(context: Context) {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE appointments ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN dayIso TEXT")
            database.execSQL("ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'Autocuidado'")
            database.execSQL("ALTER TABLE tasks ADD COLUMN emoji TEXT NOT NULL DEFAULT '📝'")
            database.execSQL("ALTER TABLE tasks ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE tasks ADD COLUMN scheduledAtEpochMillis INTEGER")
            database.execSQL("ALTER TABLE tasks ADD COLUMN durationMinutes INTEGER NOT NULL DEFAULT 30")
            database.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceRule TEXT NOT NULL DEFAULT 'NONE'")
            database.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceGroupId TEXT")
            database.execSQL("ALTER TABLE daily_records ADD COLUMN goal TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE daily_records ADD COLUMN evaluation TEXT NOT NULL DEFAULT ''")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dayIso ON tasks(dayIso)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_recurrenceGroupId ON tasks(recurrenceGroupId)")
            database.execSQL("CREATE TABLE IF NOT EXISTS routine_categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, emoji TEXT NOT NULL, isCustom INTEGER NOT NULL)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_categories_name ON routine_categories(name)")
            database.execSQL("CREATE TABLE IF NOT EXISTS checklist_templates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, taskTitles TEXT NOT NULL, category TEXT NOT NULL, createdAtEpochMillis INTEGER NOT NULL)")
        }
    }

    private val database: ProjetoDatabase = Room.databaseBuilder(
        context.applicationContext,
        ProjetoDatabase::class.java,
        "projeto_180_dias.db"
    ).addMigrations(migration1To2, migration2To3).build()

    val taskRepository = TaskRepository(database.taskDao())
    val appointmentRepository = AppointmentRepository(database.appointmentDao())
    val dailyRecordRepository = DailyRecordRepository(database.dailyRecordDao())
    val routineCategoryRepository = RoutineCategoryRepository(database.routineCategoryDao())
    val checklistTemplateRepository = ChecklistTemplateRepository(database.checklistTemplateDao())
    val settingsRepository = SettingsRepository(context)
}
