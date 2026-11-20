package br.com.projeto180dias.ui.routine

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.projeto180dias.data.local.entity.AppointmentEntity
import br.com.projeto180dias.data.local.entity.ChecklistTemplateEntity
import br.com.projeto180dias.data.local.entity.DailyRecordEntity
import br.com.projeto180dias.data.local.entity.RoutineCategoryEntity
import br.com.projeto180dias.data.local.entity.TaskEntity
import br.com.projeto180dias.data.repository.AppointmentRepository
import br.com.projeto180dias.data.repository.ChecklistTemplateRepository
import br.com.projeto180dias.data.repository.DailyRecordRepository
import br.com.projeto180dias.data.repository.RoutineCategoryRepository
import br.com.projeto180dias.data.repository.TaskRepository
import br.com.projeto180dias.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

private enum class CalendarMode { MONTH, WEEK, DAY }

@Composable
fun RoutineScreen(
    appointmentRepository: AppointmentRepository,
    taskRepository: TaskRepository,
    dailyRecordRepository: DailyRecordRepository,
    categoryRepository: RoutineCategoryRepository,
    templateRepository: ChecklistTemplateRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val zone = remember { ZoneId.systemDefault() }
    val journeyStart = remember { LocalDate.of(2026, 11, 20) }
    val journeyEnd = remember { LocalDate.of(2027, 5, 18) }
    var selectedDate by remember { mutableStateOf(journeyStart) }
    var month by remember { mutableStateOf(YearMonth.from(journeyStart)) }
    var mode by remember { mutableStateOf(CalendarMode.MONTH) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<AppointmentEntity?>(null) }
    var showAppointmentEditor by remember { mutableStateOf(false) }
    var showCategoryEditor by remember { mutableStateOf(false) }
    var showTemplateEditor by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Saúde") }

    val allAppointments by appointmentRepository.observeAll().collectAsState(initial = emptyList())
    val allTasks by taskRepository.observeAll().collectAsState(initial = emptyList())
    val dayTasks by taskRepository.observeForDay(selectedDate.toString()).collectAsState(initial = emptyList())
    val categories by categoryRepository.observeAll().collectAsState(initial = emptyList())
    val templates by templateRepository.observeAll().collectAsState(initial = emptyList())
    val dailyRecord by dailyRecordRepository.observeByDate(selectedDate.toString()).collectAsState(initial = null)
    val dailyHistory by dailyRecordRepository.observeAll().collectAsState(initial = emptyList())

    LaunchedEffect(categories) {
        if (categories.isEmpty()) {
            listOf("Saúde" to "💚", "Estudos" to "📚", "Academia" to "🏋️", "Família" to "👨‍👩‍👧", "Casa" to "🏡", "Autocuidado" to "🧴", "Hidratação" to "💧", "Lazer" to "🎨").forEach { (name, emoji) ->
                categoryRepository.create(name, emoji)
            }
        }
    }

    val dayStart = remember(selectedDate) { selectedDate.atStartOfDay(zone).toInstant().toEpochMilli() }
    val dayEnd = remember(selectedDate) { selectedDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() }
    val visibleAppointments = allAppointments.filter {
        it.startsAtEpochMillis < dayEnd && (it.endsAtEpochMillis ?: (it.startsAtEpochMillis + 1)) > dayStart
    }.sortedBy { it.startsAtEpochMillis }
    val completed = dayTasks.count { it.isCompleted }
    val total = dayTasks.size
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val eventDates = remember(allAppointments, allTasks) {
        buildSet {
            allAppointments.forEach { add(Instant.ofEpochMilli(it.startsAtEpochMillis).atZone(zone).toLocalDate()) }
            allTasks.mapNotNullTo(this) { it.dayIso?.let { day -> LocalDate.parse(day) } }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("CALENDÁRIO & CHECKLISTS", color = Copper, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text("Minha rotina", color = Forest, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("20/11/2026 a 18/05/2027 • 180 dias", color = Muted, fontSize = 11.sp)
            }
            FilledTonalIconButton(onClick = { editingTask = null; showTaskEditor = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar tarefa")
            }
        }
        Spacer(Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, tint = Forest, contentDescription = null)
                    Text(month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy")), Modifier.weight(1f).padding(start = 8.dp), color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Mês anterior") }
                    IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo mês") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { Text(it, Modifier.weight(1f), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                }
                val firstOffset = month.atDay(1).dayOfWeek.value - 1
                val cellDates = (0 until 42).map { month.atDay(1).minusDays(firstOffset.toLong()).plusDays(it.toLong()) }
                cellDates.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        week.forEach { date ->
                            val selected = date == selectedDate
                            val inMonth = date.month == month.month
                            val inJourney = !date.isBefore(journeyStart) && !date.isAfter(journeyEnd)
                            val hasEvents = date in eventDates
                            val tasksOnDate = allTasks.filter { it.dayIso == date.toString() }
                            val markerColor = when {
                                tasksOnDate.isNotEmpty() && tasksOnDate.all { it.isCompleted } -> Forest
                                hasEvents -> Copper
                                else -> Sage
                            }
                            Column(
                                Modifier.weight(1f).padding(vertical = 3.dp).clipIfNeeded(selected, inJourney).clickable {
                                    selectedDate = date
                                    if (date.month != month.month) month = YearMonth.from(date)
                                }.padding(vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(date.dayOfMonth.toString(), color = when { selected -> androidx.compose.ui.graphics.Color.White; !inMonth -> Muted.copy(alpha = .45f); inJourney -> Forest; else -> Muted }, fontSize = 12.sp, fontWeight = if (selected || date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal)
                                Box(Modifier.padding(top = 3.dp).size(5.dp).background(if (hasEvents || inJourney) markerColor else androidx.compose.ui.graphics.Color.Transparent, CircleShape))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = mode == CalendarMode.MONTH, onClick = { mode = CalendarMode.MONTH }, label = { Text("Mês") })
                    Spacer(Modifier.width(6.dp))
                    FilterChip(selected = mode == CalendarMode.WEEK, onClick = { mode = CalendarMode.WEEK }, label = { Text("Semana") })
                    Spacer(Modifier.width(6.dp))
                    FilterChip(selected = mode == CalendarMode.DAY, onClick = { mode = CalendarMode.DAY }, label = { Text("Dia") })
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { selectedDate = LocalDate.now(); month = YearMonth.now() }) { Text("Hoje") }
                }
                if (mode == CalendarMode.WEEK) {
                    val monday = selectedDate.with(java.time.DayOfWeek.MONDAY)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (0..6).forEach { offset ->
                            val date = monday.plusDays(offset.toLong())
                            FilterChip(selected = date == selectedDate, onClick = { selectedDate = date }, label = { Text(date.format(DateTimeFormatter.ofPattern("EE dd")), fontSize = 10.sp) })
                        }
                    }
                }
                if (mode == CalendarMode.DAY) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { selectedDate = selectedDate.minusDays(1); month = YearMonth.from(selectedDate) }) { Text("‹ Anterior") }
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM")), Modifier.weight(1f), color = Ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { selectedDate = selectedDate.plusDays(1); month = YearMonth.from(selectedDate) }) { Text("Próximo ›") }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM"))}", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Checklist diário", color = Muted, fontSize = 12.sp)
            }
            Text("$completed/$total", color = Forest, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(7.dp))
        LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Forest, trackColor = Sage)
        Text("${(progress * 100).toInt()}% concluído • ${total - completed} tarefas em aberto", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Tarefas", Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            TextButton(onClick = { showCategoryEditor = true }) { Text("+ Categoria") }
            TextButton(onClick = { showTemplateEditor = true }) { Text("Modelos") }
        }
        if (dayTasks.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Um espaço em branco para começar.", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("Adicione tarefas que façam sentido para este dia.", color = Muted, fontSize = 12.sp)
                    Button(onClick = { editingTask = null; showTaskEditor = true }, modifier = Modifier.padding(top = 8.dp)) { Text("Criar tarefa") }
                }
            }
        } else {
            categories.forEach { category ->
                val items = dayTasks.filter { it.category == category.name }
                if (items.isNotEmpty()) {
                    Text("${category.emoji} ${category.name}", color = Forest, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp))
                    items.forEach { task ->
                        TaskChecklistCard(
                            task = task,
                            onToggle = { checked -> scope.launch { taskRepository.setCompleted(task.id, checked) } },
                            onEdit = { editingTask = task; showTaskEditor = true },
                            onDelete = { scope.launch { taskRepository.delete(task) } },
                            onDuplicate = { scope.launch { taskRepository.duplicate(task) } }
                        )
                    }
                }
            }
            dayTasks.filter { task -> categories.none { it.name == task.category } }.forEach { task ->
                TaskChecklistCard(task, { checked -> scope.launch { taskRepository.setCompleted(task.id, checked) } }, { editingTask = task; showTaskEditor = true }, { scope.launch { taskRepository.delete(task) } }, { scope.launch { taskRepository.duplicate(task) } })
            }
        }
        if (dayTasks.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Progresso por categoria", color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            categories.forEach { category ->
                val group = dayTasks.filter { it.category == category.name }
                if (group.isNotEmpty()) {
                    val categoryDone = group.count { it.isCompleted }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${category.emoji} ${category.name}", Modifier.weight(1f), color = Muted, fontSize = 11.sp)
                        Text("$categoryDone/${group.size}", color = Forest, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(progress = { categoryDone.toFloat() / group.size.toFloat() }, Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = ForestLight, trackColor = Sage)
                }
            }
        }
        val weekStart = selectedDate.with(java.time.DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(7)
        val weeklyTasks = allTasks.filter { task -> task.dayIso?.let { day -> LocalDate.parse(day) }?.let { !it.isBefore(weekStart) && it.isBefore(weekEnd) } == true }
        Card(Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = Sage.copy(alpha = .55f)), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Resumo da semana", color = Forest, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${weeklyTasks.count { it.isCompleted }} de ${weeklyTasks.size} tarefas concluídas", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Compromissos e eventos", Modifier.weight(1f), color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { editingAppointment = null; showAppointmentEditor = true }) { Text("+ Evento") }
        }
        if (visibleAppointments.isEmpty()) Text("Nenhum compromisso neste dia.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 9.dp))
        visibleAppointments.forEach { appointment ->
            AppointmentEventCard(
                appointment, zone,
                onToggle = { done -> scope.launch { appointmentRepository.setCompleted(appointment.id, done) } },
                onEdit = { editingAppointment = appointment; showAppointmentEditor = true },
                onDelete = { scope.launch { appointmentRepository.delete(appointment) } }
            )
        }
        Spacer(Modifier.height(16.dp))
        DailyReflectionCard(
            record = dailyRecord,
            dateIso = selectedDate.toString(),
            completedCount = completed,
            repository = dailyRecordRepository
        )
        val recentHistory = dailyHistory.take(7)
        if (recentHistory.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Histórico recente", color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            recentHistory.forEach { record ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(record.dateIso, Modifier.weight(1f), color = Muted, fontSize = 11.sp)
                    if (record.goal.isNotBlank()) Text(record.goal, Modifier.weight(1f), color = Ink, fontSize = 10.sp, maxLines = 1)
                    Text("${record.completedTasksCount} concluídas", color = Forest, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (templates.isNotEmpty()) {
            Text("Modelos salvos", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            templates.forEach { template ->
                TemplateRow(template, onApply = {
                    scope.launch {
                        template.taskTitles.lines().filter(String::isNotBlank).forEach { title ->
                            taskRepository.create(TaskEntity(title = title, dayIso = selectedDate.toString(), category = template.category))
                        }
                    }
                }, onDelete = { scope.launch { templateRepository.delete(template) } })
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showTaskEditor) {
        TaskEditorDialog(
            initial = editingTask,
            date = selectedDate,
            categories = categories,
            onDismiss = { showTaskEditor = false },
            onSave = { title, description, category, emoji, notes, priority, scheduled, duration, recurrence ->
                scope.launch {
                    if (editingTask == null) {
                        val task = TaskEntity(title = title, description = description, dayIso = selectedDate.toString(), category = category, emoji = emoji, notes = notes, priority = priority, scheduledAtEpochMillis = scheduled, dueAtEpochMillis = scheduled, durationMinutes = duration, recurrenceRule = recurrence)
                        taskRepository.createWithRecurrence(task, journeyEnd)
                    } else {
                        taskRepository.update(editingTask!!.copy(title = title, description = description, category = category, emoji = emoji, notes = notes, priority = priority, scheduledAtEpochMillis = scheduled, dueAtEpochMillis = scheduled, durationMinutes = duration))
                    }
                    showTaskEditor = false
                }
            }
        )
    }
    if (showAppointmentEditor) {
        AppointmentEditorDialog(
            initial = editingAppointment,
            defaultDate = selectedDate,
            onDismiss = { showAppointmentEditor = false },
            onSave = { title, description, location, start, end ->
                scope.launch {
                    if (editingAppointment == null) {
                        appointmentRepository.create(AppointmentEntity(title = title, description = description, location = location, startsAtEpochMillis = start, endsAtEpochMillis = end))
                    } else {
                        appointmentRepository.update(editingAppointment!!.copy(title = title, description = description, location = location, startsAtEpochMillis = start, endsAtEpochMillis = end))
                    }
                    showAppointmentEditor = false
                }
            }
        )
    }
    if (showCategoryEditor) CategoryEditorDialog(onDismiss = { showCategoryEditor = false }, onSave = { name, emoji -> scope.launch { categoryRepository.create(name, emoji); showCategoryEditor = false } })
    if (showTemplateEditor) TemplateCreateDialog(onDismiss = { showTemplateEditor = false }, onSave = { name, category ->
        scope.launch { templateRepository.create(name, category, dayTasks.map { it.title }); showTemplateEditor = false }
    }, categories = categories, hasTasks = dayTasks.isNotEmpty())
}

@Composable
private fun Modifier.clipIfNeeded(selected: Boolean, inJourney: Boolean): Modifier = this.then(
    Modifier.background(if (selected) Forest else if (inJourney) Sage.copy(alpha = .28f) else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(10.dp))
)

@Composable
private fun TaskChecklistCard(task: TaskEntity, onToggle: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onDuplicate: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 7.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(15.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onToggle)
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text("${task.emoji} ${task.title}", color = if (task.isCompleted) Muted else Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                val time = task.scheduledAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")) }
                Text(listOfNotNull(time, "${task.durationMinutes} min", task.priority.lowercase().replaceFirstChar { it.uppercase() }).joinToString(" • "), color = Copper, fontSize = 10.sp)
                if (task.notes.isNotBlank()) Text(task.notes, color = Muted, fontSize = 11.sp, maxLines = 2)
            }
            IconButton(onClick = onDuplicate) { Icon(Icons.Filled.FileCopy, contentDescription = "Duplicar", tint = Forest) }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Forest) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = Copper) }
        }
    }
}

@Composable
private fun AppointmentEventCard(item: AppointmentEntity, zone: ZoneId, onToggle: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val start = Instant.ofEpochMilli(item.startsAtEpochMillis).atZone(zone)
    val end = item.endsAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone) }
    Card(Modifier.fillMaxWidth().padding(bottom = 6.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.isCompleted, onCheckedChange = onToggle)
            Column(Modifier.weight(1f)) {
                Text("📅 ${item.title}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("${start.format(DateTimeFormatter.ofPattern("HH:mm"))} – ${end?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "sem horário final"}", color = Copper, fontSize = 11.sp)
                if (item.description.isNotBlank()) Text(item.description, color = Muted, fontSize = 11.sp)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Editar evento", tint = Forest) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir evento", tint = Copper) }
        }
    }
}

@Composable
private fun DailyReflectionCard(record: DailyRecordEntity?, dateIso: String, completedCount: Int, repository: DailyRecordRepository) {
    val scope = rememberCoroutineScope()
    var goal by remember(dateIso, record?.id) { mutableStateOf(record?.goal.orEmpty()) }
    var evaluation by remember(dateIso, record?.id) { mutableStateOf(record?.evaluation.orEmpty()) }
    var notes by remember(dateIso, record?.id) { mutableStateOf(record?.notes.orEmpty()) }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Meta e avaliação do dia", color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            OutlinedTextField(goal, { goal = it }, Modifier.fillMaxWidth(), label = { Text("Minha meta") }, singleLine = true)
            OutlinedTextField(evaluation, { evaluation = it }, Modifier.fillMaxWidth(), label = { Text("Como foi meu dia?") }, minLines = 2)
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Anotações") }, minLines = 2)
            Button(onClick = {
                scope.launch {
                    repository.save((record ?: DailyRecordEntity(dateIso = dateIso)).copy(dateIso = dateIso, goal = goal, evaluation = evaluation, notes = notes, completedTasksCount = completedCount))
                }
            }, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Salvar registro")
            }
        }
    }
}

@Composable
private fun TemplateRow(template: ChecklistTemplateEntity, onApply: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(template.name, color = Ink, fontWeight = FontWeight.SemiBold); Text("${template.category} • ${template.taskTitles.lines().size} itens", color = Muted, fontSize = 10.sp) }
        TextButton(onClick = onApply) { Text("Aplicar") }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir modelo", tint = Copper) }
    }
}

@Composable
private fun TaskEditorDialog(initial: TaskEntity?, date: LocalDate, categories: List<RoutineCategoryEntity>, onDismiss: () -> Unit, onSave: (String, String, String, String, String, String, Long?, Int, String) -> Unit) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: categories.firstOrNull()?.name ?: "Autocuidado") }
    var emoji by remember(initial?.id) { mutableStateOf(initial?.emoji ?: "📝") }
    var notes by remember(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }
    var priority by remember(initial?.id) { mutableStateOf(initial?.priority ?: "NORMAL") }
    var duration by remember(initial?.id) { mutableIntStateOf(initial?.durationMinutes ?: 30) }
    var recurrence by remember(initial?.id) { mutableStateOf("NONE") }
    var time by remember(initial?.id) { mutableStateOf(initial?.scheduledAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() }) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nova tarefa" else "Editar tarefa") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Tarefa *") }, singleLine = true)
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrição") })
                Text("Categoria", color = Muted, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    categories.forEach { cat -> FilterChip(selected = category == cat.name, onClick = { category = cat.name; emoji = cat.emoji }, label = { Text("${cat.emoji} ${cat.name}") }) }
                }
                OutlinedTextField(emoji, { emoji = it }, Modifier.fillMaxWidth(), label = { Text("Emoji") }, singleLine = true)
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Observações") }, minLines = 2)
                Text("Prioridade", color = Muted, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("LOW" to "Baixa", "NORMAL" to "Normal", "HIGH" to "Alta").forEach { (value, label) -> FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(label) }) } }
                OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time?.hour ?: 9, time?.minute ?: 0, true).show() }, Modifier.fillMaxWidth()) { Text("Horário: ${time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Não definido"}") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Duração", Modifier.weight(1f), color = Muted)
                    OutlinedButton(onClick = { duration = (duration - 15).coerceAtLeast(5) }) { Text("−") }
                    Text("$duration min", Modifier.padding(horizontal = 10.dp), color = Ink)
                    OutlinedButton(onClick = { duration = (duration + 15).coerceAtMost(1440) }) { Text("+") }
                }
                if (initial == null) {
                    Text("Repetição até 18/05/2027", color = Muted, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("NONE" to "Não", "DAILY" to "Diária", "WEEKLY" to "Semanal", "WEEKDAYS" to "Dias úteis").forEach { (value, label) -> FilterChip(selected = recurrence == value, onClick = { recurrence = value }, label = { Text(label, fontSize = 10.sp) }) } }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = { TextButton(onClick = {
            if (title.isBlank()) error = "Informe o nome da tarefa."
            else {
                val timestamp = time?.let { LocalDateTime.of(date, it).atZone(zone).toInstant().toEpochMilli() }
                onSave(title.trim(), description.trim(), category, emoji.ifBlank { "📝" }, notes.trim(), priority, timestamp, duration, recurrence)
            }
        }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun CategoryEditorDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📌") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nova categoria") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nome") })
            OutlinedTextField(emoji, { emoji = it }, label = { Text("Emoji") })
        }
    }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name, emoji) }) { Text("Criar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun TemplateCreateDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit, categories: List<RoutineCategoryEntity>, hasTasks: Boolean) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull()?.name ?: "Autocuidado") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Salvar checklist como modelo") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!hasTasks) Text("Adicione tarefas ao dia antes de criar um modelo.", color = Muted)
            OutlinedTextField(name, { name = it }, label = { Text("Nome do modelo") })
            categories.forEach { FilterChip(selected = category == it.name, onClick = { category = it.name }, label = { Text("${it.emoji} ${it.name}") }) }
        }
    }, confirmButton = { TextButton(enabled = hasTasks && name.isNotBlank(), onClick = { onSave(name, category) }) { Text("Salvar modelo") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}



@Composable
private fun AppointmentEditorDialog(
    initial: AppointmentEntity?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long, Long) -> Unit
) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    val initialStart = remember(initial?.id) { initial?.startsAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime() } ?: LocalDateTime.of(defaultDate, LocalTime.of(9, 0)) }
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var location by remember(initial?.id) { mutableStateOf(initial?.location.orEmpty()) }
    var date by remember(initial?.id) { mutableStateOf(initialStart.toLocalDate()) }
    var startTime by remember(initial?.id) { mutableStateOf(initialStart.toLocalTime()) }
    var durationMinutes by remember(initial?.id) {
        val end = initial?.endsAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime() }
        mutableIntStateOf(if (end != null) java.time.Duration.between(initialStart, end).toMinutes().toInt().coerceAtLeast(5) else 60)
    }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Novo compromisso" else "Editar compromisso") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Título *") }, singleLine = true)
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrição") })
                OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Local") }, singleLine = true)
                OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }, Modifier.fillMaxWidth()) { Text("Data: ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}") }
                OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> startTime = LocalTime.of(h, m) }, startTime.hour, startTime.minute, true).show() }, Modifier.fillMaxWidth()) { Text("Início: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Duração", Modifier.weight(1f), color = Muted)
                    OutlinedButton(onClick = { durationMinutes = (durationMinutes - 15).coerceAtLeast(5) }) { Text("−") }
                    Text("$durationMinutes min", Modifier.padding(horizontal = 8.dp), color = Ink)
                    OutlinedButton(onClick = { durationMinutes = (durationMinutes + 15).coerceAtMost(1440) }) { Text("+") }
                }
                Text("Término: ${startTime.plusMinutes(durationMinutes.toLong()).format(DateTimeFormatter.ofPattern("HH:mm"))}", color = Muted, fontSize = 11.sp)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
            }
        },
        confirmButton = { TextButton(onClick = {
            if (title.isBlank()) error = "Informe o título do compromisso."
            else {
                val start = LocalDateTime.of(date, startTime).atZone(zone).toInstant().toEpochMilli()
                val end = LocalDateTime.of(date, startTime).plusMinutes(durationMinutes.toLong()).atZone(zone).toInstant().toEpochMilli()
                onSave(title.trim(), description.trim(), location.trim(), start, end)
            }
        }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
