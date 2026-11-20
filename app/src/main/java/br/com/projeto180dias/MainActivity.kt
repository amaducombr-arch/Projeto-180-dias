package br.com.projeto180dias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.projeto180dias.di.AppContainer
import br.com.projeto180dias.ui.home.HomeScreen
import br.com.projeto180dias.ui.theme.Projeto180DiasTheme

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appContainer = AppContainer(applicationContext)
        setContent {
            Projeto180DiasTheme {
                HomeScreen(
                    taskRepository = appContainer.taskRepository,
                    appointmentRepository = appContainer.appointmentRepository,
                    dailyRecordRepository = appContainer.dailyRecordRepository,
                    categoryRepository = appContainer.routineCategoryRepository,
                    templateRepository = appContainer.checklistTemplateRepository
                )
            }
        }
    }
}
