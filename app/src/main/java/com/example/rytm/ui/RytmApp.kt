package com.example.rytm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.example.rytm.data.Task
import com.example.rytm.data.WeekPlan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val Charcoal = Color(0xFF181918)
private val Paper = Color(0xFFF7F5ED)
private val Ink = Color(0xFF232826)
private val Muted = Color(0xFF6C726E)
private val Green = Color(0xFF1E7560)
private val Orange = Color(0xFFCA521A)
private val Blue = Color(0xFF3563AB)
private val Purple = Color(0xFF7E4DA7)
private val Polish = Locale.forLanguageTag("pl-PL")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun RytmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Green,
            onPrimary = Color.White,
            background = Charcoal,
            surface = Paper,
            onSurface = Ink
        ),
        typography = MaterialTheme.typography.copy(
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        ),
        content = content
    )
}

@Composable
fun RytmApp(state: RytmUiState, viewModel: RytmViewModel) {
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var showNewTask by remember { mutableStateOf(false) }
    var planBeingEdited by remember { mutableStateOf<WeekPlan?>(null) }
    var showNewPlan by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(
                    RytmScreen.TODAY to "✓\nDziś",
                    RytmScreen.WEEK to "▣\nTydzień",
                    RytmScreen.PLANS to "≡\nPlany"
                ).forEach { (screen, label) ->
                    NavigationBarItem(
                        selected = state.screen == screen,
                        onClick = { viewModel.showScreen(screen) },
                        icon = {
                            NavigationIcon(
                                type = screen,
                                tint = if (state.screen == screen) Green else Muted
                            )
                        },
                        label = { Text(label.substringAfter('\n'), fontSize = 12.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(0.dp),
            color = Paper
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (state.screen) {
                    RytmScreen.TODAY -> TodayScreen(state, viewModel, { taskBeingEdited = it }, { showNewTask = true })
                    RytmScreen.WEEK -> WeekScreen(state, viewModel, { taskBeingEdited = it }, { showNewTask = true })
                    RytmScreen.PLANS -> PlansScreen(
                        state,
                        viewModel,
                        onCreate = { showNewPlan = true },
                        onEdit = { planBeingEdited = it }
                    )
                }
            }
        }
    }

    if (showNewTask) {
        TaskEditorDialog(
            task = null,
            defaultDay = state.selectedDate.dayOfWeek.value,
            onDismiss = { showNewTask = false },
            onSave = { title, day, start, end, color, note ->
                viewModel.createTask(title, day, start, end, color, note) { createdTask ->
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Zadanie dodane",
                            actionLabel = "Cofnij",
                            duration = SnackbarDuration.Short
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.deleteTask(createdTask)
                        }
                    }
                }
                showNewTask = false
            }
        )
    }
    taskBeingEdited?.let { task ->
        TaskEditorDialog(
            task = task,
            defaultDay = task.dayOfWeek,
            onDismiss = { taskBeingEdited = null },
            onDelete = {
                viewModel.deleteTask(task)
                taskBeingEdited = null
            },
            onSave = { title, day, start, end, color, note ->
                viewModel.updateTask(task.copy(title = title, dayOfWeek = day, startTime = start, endTime = end, colorHex = color, note = note))
                taskBeingEdited = null
            }
        )
    }
    if (showNewPlan) {
        PlanEditorDialog(
            plan = null,
            onDismiss = { showNewPlan = false },
            onSave = { name, color ->
                viewModel.createPlan(name, color)
                showNewPlan = false
            }
        )
    }
    planBeingEdited?.let { plan ->
        PlanEditorDialog(
            plan = plan,
            onDismiss = { planBeingEdited = null },
            onDelete = {
                viewModel.deletePlan(plan)
                planBeingEdited = null
            },
            onSave = { name, color ->
                viewModel.updatePlan(plan.copy(name = name, colorHex = color))
                planBeingEdited = null
            }
        )
    }
}

@Composable
private fun NavigationIcon(type: RytmScreen, tint: Color) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.4.dp.toPx()
        val lineStyle = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (type) {
            RytmScreen.TODAY -> {
                drawCircle(tint, radius = 6.dp.toPx(), style = lineStyle)
                drawLine(
                    tint,
                    androidx.compose.ui.geometry.Offset(6.dp.toPx(), 9.dp.toPx()),
                    androidx.compose.ui.geometry.Offset(8.dp.toPx(), 11.dp.toPx()),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    tint,
                    androidx.compose.ui.geometry.Offset(8.dp.toPx(), 11.dp.toPx()),
                    androidx.compose.ui.geometry.Offset(12.dp.toPx(), 6.5.dp.toPx()),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
            RytmScreen.WEEK -> {
                drawRoundRect(
                    tint,
                    topLeft = androidx.compose.ui.geometry.Offset(2.dp.toPx(), 4.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 11.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                    style = lineStyle
                )
                drawLine(tint, androidx.compose.ui.geometry.Offset(2.dp.toPx(), 7.dp.toPx()), androidx.compose.ui.geometry.Offset(16.dp.toPx(), 7.dp.toPx()), strokeWidth = stroke)
                drawLine(tint, androidx.compose.ui.geometry.Offset(6.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.Offset(6.dp.toPx(), 6.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, androidx.compose.ui.geometry.Offset(12.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.Offset(12.dp.toPx(), 6.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            RytmScreen.PLANS -> {
                listOf(4.dp, 8.dp, 12.dp).forEach { y ->
                    drawCircle(tint, radius = 0.8.dp.toPx(), center = androidx.compose.ui.geometry.Offset(3.dp.toPx(), y.toPx()))
                    drawLine(tint, androidx.compose.ui.geometry.Offset(6.dp.toPx(), y.toPx()), androidx.compose.ui.geometry.Offset(15.dp.toPx(), y.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(
    state: RytmUiState,
    viewModel: RytmViewModel,
    onEdit: (Task) -> Unit,
    onAdd: () -> Unit
) {
    val dateText = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Polish))
        .replaceFirstChar { it.titlecase(Polish) }
    val todayTasks = state.tasks.filter { it.dayOfWeek == state.selectedDate.dayOfWeek.value }
    Text(dateText, color = Muted, fontSize = 13.sp)
    Text("Dziś", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    Row(verticalAlignment = Alignment.CenterVertically) {
        state.activePlan?.let {
            PlanPill(it)
            Spacer(Modifier.width(12.dp))
        }
        Text("${todayTasks.count { it.isCompleted }} z ${todayTasks.size} wykonane", color = Muted, fontSize = 13.sp)
    }
    Spacer(Modifier.height(14.dp))
    if (todayTasks.isEmpty()) {
        EmptyState("Brak zadań na dziś", "Dodaj pierwsze zadanie przyciskiem +", onAdd)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(todayTasks, key = { it.id }) { task ->
                TaskRow(task, onChecked = { viewModel.setTaskCompleted(task, it) }, onClick = { onEdit(task) })
            }
        }
    }
}

@Composable
private fun WeekScreen(state: RytmUiState, viewModel: RytmViewModel, onEdit: (Task) -> Unit, onAdd: () -> Unit) {
    val monday = state.selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = monday.plusDays(6)
    Text(
        "${monday.dayOfMonth} – ${sunday.dayOfMonth} września · Plan: ${state.activePlan?.name ?: "brak"}",
        color = Muted,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 12.dp)
    )
    Text("Tydzień", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (0..6).forEach { offset ->
            val day = monday.plusDays(offset.toLong())
            val selected = day == state.selectedDate
            val shortName = listOf("Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nd")[offset]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (selected) Green else Color.White,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { viewModel.selectDate(day) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(shortName, color = if (selected) Color.White else Ink, fontSize = 10.sp, lineHeight = 13.sp)
                Text("${day.dayOfMonth}", color = if (selected) Color.White else Ink, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    val selectedTasks = state.tasks
        .filter { it.dayOfWeek == state.selectedDate.dayOfWeek.value }
        .sortedBy { it.startTime }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(selectedTasks, key = { it.id }) { task ->
            WeekTaskRow(
                task = task,
                onClick = { onEdit(task) }
            )
        }
        item {
            OutlinedButton(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Text("Dodaj Zadanie", color = Green, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun WeekTaskRow(task: Task, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            task.startTime.format(timeFormatter),
            color = Muted,
            fontSize = 10.sp,
            modifier = Modifier.width(38.dp)
        )
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(task.title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${task.startTime.format(timeFormatter)} – ${task.endTime.format(timeFormatter)}",
                        color = Muted,
                        fontSize = 10.sp
                    )
                }
                Box(Modifier.size(9.dp).background(parseColor(task.colorHex), RoundedCornerShape(50)))
            }
        }
    }
}

@Composable
private fun PlansScreen(state: RytmUiState, viewModel: RytmViewModel, onCreate: () -> Unit, onEdit: (WeekPlan) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("Plany", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
            Text("Nowy plan", fontSize = 11.sp)
        }
    }
    Spacer(Modifier.height(14.dp))
    if (state.plans.isEmpty()) {
        EmptyState("Nie masz jeszcze planów", "Utwórz plan tygodnia", onCreate)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.plans, key = { it.id }) { plan ->
                PlanCard(
                    plan = plan,
                    active = state.activePlan?.id == plan.id,
                    onUse = { viewModel.usePlanThisWeek(plan) },
                    onEdit = { onEdit(plan) }
                )
            }
        }
    }
}

@Composable
private fun PlanCard(plan: WeekPlan, active: Boolean, onUse: () -> Unit, onEdit: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(9.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(parseColor(plan.colorHex), RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(plan.name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(if (active) "Aktywny w tym tygodniu" else "Nieprzypisany", color = Muted, fontSize = 13.sp)
            }
            if (!active) TextButton(onClick = onUse) { Text("Użyj", color = Green, fontSize = 13.sp) }
            TextButton(onClick = onEdit) { Text("Edytuj", color = Muted, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun PlanPill(plan: WeekPlan) {
    Surface(color = parseColor(plan.colorHex), shape = RoundedCornerShape(12.dp)) {
        Text("Plan: ${plan.name}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
private fun TaskRow(task: Task, onChecked: (Boolean) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onChecked)
            Column(Modifier.weight(1f)) {
                Text(task.title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${task.startTime.format(timeFormatter)} – ${task.endTime.format(timeFormatter)}", color = Muted, fontSize = 12.sp)
            }
            Box(Modifier.size(10.dp).background(parseColor(task.colorHex), RoundedCornerShape(50)))
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun CompactTaskRow(task: Task, onChecked: (Boolean) -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isCompleted, onCheckedChange = onChecked, modifier = Modifier.size(38.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("${task.startTime.format(timeFormatter)} – ${task.endTime.format(timeFormatter)}", color = Muted, fontSize = 12.sp)
        }
        Box(Modifier.size(10.dp).background(parseColor(task.colorHex), RoundedCornerShape(50)))
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, onAdd: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onAdd) { Text("Dodaj", color = Green) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanEditorDialog(
    plan: WeekPlan?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (String, String) -> Unit
) {
    var name by remember(plan) { mutableStateOf(plan?.name ?: "") }
    var color by remember(plan) { mutableStateOf(plan?.colorHex ?: "#1E7560") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (plan == null) "Nowy plan" else "Edycja planu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, singleLine = true)
                Text("Kolor", color = Muted, fontSize = 14.sp)
                ColorChoices(color) { color = it }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, color) }, enabled = name.isNotBlank()) { Text("Zapisz", color = Green) } },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Usuń", color = Orange) }
                TextButton(onClick = onDismiss) { Text("Anuluj", color = Muted) }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorDialog(
    task: Task?,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (String, Int, LocalTime, LocalTime, String, String) -> Unit
) {
    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var note by remember(task) { mutableStateOf(task?.note ?: "") }
    var day by remember(task) { mutableStateOf(task?.dayOfWeek ?: defaultDay) }
    var start by remember(task) { mutableStateOf(task?.startTime?.format(timeFormatter) ?: "09:00") }
    var end by remember(task) { mutableStateOf(task?.endTime?.format(timeFormatter) ?: "10:00") }
    var color by remember(task) { mutableStateOf(task?.colorHex ?: "#1E7560") }
    var titleError by remember(task) { mutableStateOf(false) }
    var timeError by remember(task) { mutableStateOf<String?>(null) }
    var editingTime by remember { mutableStateOf<TimeField?>(null) }
    var selectedDuration by remember(task) {
        mutableStateOf(
            task?.let { durationMinutes(it.startTime, it.endTime) }
        )
    }
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (task == null) titleFocusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Nowe zadanie" else "Edycja zadania") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Co chcesz zrobić?") },
                    placeholder = { Text("Np. Nauka angielskiego") },
                    singleLine = true,
                    isError = titleError,
                    supportingText = { if (titleError) Text("Wpisz tytuł zadania") },
                    modifier = Modifier.focusRequester(titleFocusRequester)
                )
                Text("Dzień tygodnia", color = Muted, fontSize = 14.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd").forEachIndexed { index, label ->
                        FilterChip(
                            selected = day == index + 1,
                            onClick = { day = index + 1 },
                            label = { Text(label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { editingTime = TimeField.START },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Od", color = Muted, fontSize = 11.sp)
                            Text(start, color = Ink, fontSize = 17.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            selectedDuration = null
                            editingTime = TimeField.END
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Do", color = Muted, fontSize = 11.sp)
                            Text(end, color = Ink, fontSize = 17.sp)
                        }
                    }
                }
                Text("Czas trwania", color = Muted, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30, 45, 60).forEach { minutes ->
                        FilterChip(
                            selected = selectedDuration == minutes,
                            onClick = {
                                selectedDuration = minutes
                                end = LocalTime.parse(start)
                                    .plusMinutes(minutes.toLong())
                                    .format(timeFormatter)
                                timeError = null
                            },
                            label = { Text("$minutes min") }
                        )
                    }
                }
                if (timeError != null) {
                    Text(timeError!!, color = Orange, fontSize = 12.sp)
                }
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notatka (opcjonalnie)") },
                    minLines = 2,
                    maxLines = 4
                )
                ColorChoices(color) { color = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val startTime = LocalTime.parse(start)
                val endTime = LocalTime.parse(end)
                when {
                    title.isBlank() -> titleError = true
                    !endTime.isAfter(startTime) -> timeError = "Godzina zakończenia musi być późniejsza niż rozpoczęcia"
                    else -> onSave(title.trim(), day, startTime, endTime, color, note.trim())
                }
            }) { Text("Zapisz", color = Green) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Usuń", color = Orange) }
                TextButton(onClick = onDismiss) { Text("Anuluj", color = Muted) }
            }
        }
    )

    editingTime?.let { field ->
        key(field) {
            val current = LocalTime.parse(if (field == TimeField.START) start else end)
            val pickerState = rememberTimePickerState(current.hour, current.minute, is24Hour = true)
            Dialog(
                onDismissRequest = { editingTime = null },
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Wybierz godzinę", color = Ink, fontWeight = FontWeight.Bold)
                        TimePicker(state = pickerState)
                        Row {
                            TextButton(onClick = { editingTime = null }) {
                                Text("Anuluj", color = Muted)
                            }
                            TextButton(onClick = {
                                val selected = LocalTime.of(pickerState.hour, pickerState.minute)
                                if (field == TimeField.START) {
                                    start = selected.format(timeFormatter)
                                    selectedDuration?.let { minutes ->
                                        end = selected.plusMinutes(minutes.toLong()).format(timeFormatter)
                                    }
                                } else {
                                    end = selected.format(timeFormatter)
                                    selectedDuration = null
                                }
                                timeError = null
                                editingTime = null
                            }) {
                                Text("OK", color = Green)
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class TimeField { START, END }

private fun durationMinutes(start: LocalTime, end: LocalTime): Int? {
    val minutes = java.time.Duration.between(start, end).toMinutes().toInt()
    return minutes.takeIf { it == 30 || it == 45 || it == 60 }
}

@Composable
private fun ColorChoices(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("#1E7560", "#CA521A", "#3563AB", "#7E4DA7").forEach { value ->
            Box(
                Modifier
                    .size(25.dp)
                    .background(parseColor(value), RoundedCornerShape(50))
                    .clickable { onSelect(value) }
                    .padding(if (selected == value) 3.dp else 0.dp)
            )
        }
    }
}

private fun parseColor(value: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrDefault(Green)
