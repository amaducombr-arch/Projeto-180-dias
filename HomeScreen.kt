package br.com.projeto180dias.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.projeto180dias.data.local.entity.AppointmentEntity
import br.com.projeto180dias.data.repository.AppointmentRepository
import br.com.projeto180dias.data.repository.TaskRepository
import br.com.projeto180dias.data.repository.DailyRecordRepository
import br.com.projeto180dias.data.repository.RoutineCategoryRepository
import br.com.projeto180dias.data.repository.ChecklistTemplateRepository
import br.com.projeto180dias.data.local.entity.TaskEntity
import br.com.projeto180dias.ui.routine.RoutineScreen
import br.com.projeto180dias.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(taskRepository: TaskRepository, appointmentRepository: AppointmentRepository, dailyRecordRepository: DailyRecordRepository, categoryRepository: RoutineCategoryRepository, templateRepository: ChecklistTemplateRepository) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(containerColor = SurfaceWhite, tonalElevation = 0.dp) {
                val items = listOf(
                    Triple("Hoje", Icons.Filled.Home, 0),
                    Triple("Rotina", Icons.Filled.CalendarMonth, 1),
                    Triple("Hábitos", Icons.Filled.CheckCircle, 2),
                    Triple("Evolução", Icons.Filled.Insights, 3),
                    Triple("Perfil", Icons.Filled.Person, 4)
                )
                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Forest, selectedTextColor = Forest, indicatorColor = Sage, unselectedIconColor = Muted, unselectedTextColor = Muted)
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> Dashboard(taskRepository, appointmentRepository)
                1 -> RoutineScreen(appointmentRepository, taskRepository, dailyRecordRepository, categoryRepository, templateRepository)
                else -> PlaceholderTab(selectedTab)
            }
        }
    }
}

@Composable
private fun Dashboard(taskRepository: TaskRepository, appointmentRepository: AppointmentRepository) {
    val scope = rememberCoroutineScope()
    val appointments by appointmentRepository.observeAll().collectAsState(initial = emptyList())
    val today = LocalDate.now()
    val zone = remember { ZoneId.systemDefault() }
    val start = remember(today) { today.atStartOfDay(zone).toInstant().toEpochMilli() }
    val end = remember(today) { today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() }
    val todayAppointments = appointments.filter { it.startsAtEpochMillis < end && (it.endsAtEpochMillis ?: (it.startsAtEpochMillis + 1)) > start }.sortedBy { it.startsAtEpochMillis }
    val todayTasks by taskRepository.observeForDay(today.toString()).collectAsState(initial = emptyList())
    val completedToday = todayAppointments.count { it.isCompleted }
    val pendingTasks = todayTasks.count { !it.isCompleted }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(22.dp))
        Header()
        Spacer(Modifier.height(22.dp))
        JourneyCard()
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Seu dia", Modifier.weight(1f), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Compromissos", todayAppointments.size.toString(), "hoje", Modifier.weight(1f))
            MetricCard("Concluídos", "$completedToday/${todayAppointments.size}", "na agenda", Modifier.weight(1f))
            MetricCard("Tarefas", pendingTasks.toString(), "pendentes", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Text("Próximos compromissos", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (todayAppointments.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(17.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Nenhum compromisso para hoje.", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("Use a aba Rotina para planejar seus horários.", color = Muted, fontSize = 12.sp)
                }
            }
        } else {
            todayAppointments.take(5).forEach { AppointmentSummary(it, zone) }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Checklist de hoje", Modifier.weight(1f), color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("${todayTasks.count { it.isCompleted }}/${todayTasks.size}", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        if (todayTasks.isEmpty()) {
            Text("Seu checklist ainda está vazio. Adicione tarefas na aba Rotina.", color = Muted, fontSize = 12.sp)
        } else {
            todayTasks.take(8).forEach { task ->
                DashboardTaskRow(task) { checked ->
                    scope.launch { taskRepository.setCompleted(task.id, checked) }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("A constância vale mais que a pressa.", Modifier.fillMaxWidth().padding(bottom = 24.dp), color = ForestLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("PROJETO", color = Copper, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("180 DIAS", color = Forest, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Text("Sua reconstrução começa hoje.", color = Muted, fontSize = 13.sp)
        }
        Box(Modifier.size(46.dp).clip(CircleShape).background(Sage), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Spa, contentDescription = "Símbolo do Projeto 180 Dias", tint = Forest, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun JourneyCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Forest)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SUA JORNADA", color = Color(0xFFD8C5B9), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.weight(1f))
                Surface(color = Color(0xFF40594F), shape = RoundedCornerShape(50)) {
                    Text("DIA 01", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("0", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp)
                Text(" / 180 dias", color = Color(0xFFD8DED8), fontSize = 14.sp, modifier = Modifier.padding(start = 7.dp, bottom = 7.dp))
            }
            LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Color(0xFFD6A092), trackColor = Color(0xFF52675E))
            Spacer(Modifier.height(10.dp))
            Text("Todo recomeço merece respeito. Hoje, faça o possível.", color = Color(0xFFE4EAE5), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(11.dp)) {
            Text(title, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = Forest, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Copper, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AppointmentSummary(item: AppointmentEntity, zone: ZoneId) {
    val time = Instant.ofEpochMilli(item.startsAtEpochMillis).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm"))
    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(15.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(time, color = Copper, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(item.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (item.location.isNotBlank()) Text(item.location, color = Muted, fontSize = 11.sp)
            }
            if (item.isCompleted) Icon(Icons.Filled.CheckCircle, contentDescription = "Concluído", tint = Forest)
        }
    }
}

@Composable
private fun DashboardTaskRow(task: TaskEntity, onToggle: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 7.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onToggle)
            Column(Modifier.weight(1f)) {
                Text("${task.emoji} ${task.title}", color = if (task.isCompleted) Muted else Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(task.category, color = Copper, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun PlaceholderTab(index: Int) {
    val title = when (index) { 2 -> "Hábitos"; 3 -> "Evolução"; else -> "Perfil" }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.Construction, contentDescription = null, tint = Copper, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Forest, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Esta área será desenvolvida em uma próxima etapa.", color = Muted, fontSize = 13.sp)
    }
}
