package com.example.rytm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rytm.data.RytmRepository
import com.example.rytm.data.Task
import com.example.rytm.data.WeekPlan
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

enum class RytmScreen { TODAY, WEEK, PLANS }

data class RytmUiState(
    val screen: RytmScreen = RytmScreen.TODAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val plans: List<WeekPlan> = emptyList(),
    val activePlan: WeekPlan? = null,
    val tasks: List<Task> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class RytmViewModel(private val repository: RytmRepository) : ViewModel() {
    private val screen = MutableStateFlow(RytmScreen.TODAY)
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val activePlan = selectedDate.flatMapLatest(repository::activePlanFor)
    private val activeTasks = activePlan.flatMapLatest { plan ->
        plan?.let { repository.tasksForPlan(it.id) } ?: flowOf(emptyList())
    }

    val uiState: StateFlow<RytmUiState> = combine(
        screen,
        selectedDate,
        repository.allPlans,
        activePlan,
        activeTasks
    ) { currentScreen, date, plans, plan, tasks ->
        RytmUiState(currentScreen, date, plans, plan, tasks)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RytmUiState(selectedDate = selectedDate.value)
    )

    init {
        viewModelScope.launch { repository.seedIfNeeded() }
    }

    fun showScreen(value: RytmScreen) {
        screen.value = value
    }

    fun selectDate(value: LocalDate) {
        selectedDate.value = value
    }

    fun moveWeek(offset: Long) {
        selectedDate.value = selectedDate.value.plusWeeks(offset)
    }

    fun setTaskCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch { repository.updateTask(task.copy(isCompleted = completed)) }
    }

    fun createPlan(name: String, colorHex: String, onCreated: (Long) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createPlan(name, colorHex)
            repository.assignPlanTo(id, selectedDate.value)
            onCreated(id)
        }
    }

    fun updatePlan(plan: WeekPlan) {
        if (plan.name.isBlank()) return
        viewModelScope.launch { repository.updatePlan(plan) }
    }

    fun deletePlan(plan: WeekPlan) {
        viewModelScope.launch { repository.deletePlan(plan) }
    }

    fun usePlanThisWeek(plan: WeekPlan) {
        viewModelScope.launch { repository.assignPlanTo(plan.id, selectedDate.value) }
    }

    fun createTask(
        title: String,
        dayOfWeek: Int,
        startTime: LocalTime,
        endTime: LocalTime,
        colorHex: String,
        note: String = ""
    ) {
        val plan = uiState.value.activePlan ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createTask(
                Task(
                    planId = plan.id,
                    title = title,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    colorHex = colorHex,
                    note = note
                )
            )
        }
    }

    fun updateTask(task: Task) {
        if (task.title.isBlank()) return
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}

class RytmViewModelFactory(private val repository: RytmRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RytmViewModel::class.java))
        return RytmViewModel(repository) as T
    }
}
