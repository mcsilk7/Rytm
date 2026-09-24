package com.example.rytm.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.IsoFields
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RytmRepository(private val database: RytmDatabase) {
    private val plans = database.weekPlanDao()
    private val tasks = database.taskDao()
    private val assignments = database.weekAssignmentDao()

    val allPlans: Flow<List<WeekPlan>> = plans.observeAll()

    fun activePlanFor(date: LocalDate): Flow<WeekPlan?> {
        val year = date.get(IsoFields.WEEK_BASED_YEAR)
        val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return combine(assignments.observeForWeek(year, week), allPlans) { assignment, available ->
            available.firstOrNull { it.id == assignment?.planId } ?: available.firstOrNull()
        }
    }

    fun tasksForPlan(planId: Long): Flow<List<Task>> = tasks.observeForPlan(planId)

    suspend fun seedIfNeeded() {
        if (plans.count() > 0) return
        val school = WeekPlan(name = "Szkoła", colorHex = "#1E7560")
        val schoolId = plans.insert(school)
        val examples = listOf(
            Task(planId = schoolId, title = "Poranna rozgrzewka", dayOfWeek = DayOfWeek.THURSDAY.value, startTime = LocalTime.of(7, 0), endTime = LocalTime.of(7, 30), colorHex = "#CA521A", isCompleted = true),
            Task(planId = schoolId, title = "Dojazd do szkoły", dayOfWeek = DayOfWeek.THURSDAY.value, startTime = LocalTime.of(7, 40), endTime = LocalTime.of(8, 0), colorHex = "#3563AB", isCompleted = true),
            Task(planId = schoolId, title = "Lekcje", dayOfWeek = DayOfWeek.THURSDAY.value, startTime = LocalTime.of(8, 0), endTime = LocalTime.of(14, 0), colorHex = "#1E7560"),
            Task(planId = schoolId, title = "Nauka angielskiego", dayOfWeek = DayOfWeek.THURSDAY.value, startTime = LocalTime.of(16, 0), endTime = LocalTime.of(17, 0), colorHex = "#7E4DA7"),
            Task(planId = schoolId, title = "Trening", dayOfWeek = DayOfWeek.THURSDAY.value, startTime = LocalTime.of(18, 30), endTime = LocalTime.of(19, 30), colorHex = "#CA521A")
        )
        examples.forEach { tasks.insert(it) }
        val today = LocalDate.now()
        assignments.insert(
            WeekAssignment(
                planId = schoolId,
                isoYear = today.get(IsoFields.WEEK_BASED_YEAR),
                isoWeek = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            )
        )
    }

    suspend fun createPlan(name: String, colorHex: String): Long =
        plans.insert(WeekPlan(name = name.trim(), colorHex = colorHex))

    suspend fun updatePlan(plan: WeekPlan) = plans.update(plan.copy(name = plan.name.trim()))

    suspend fun deletePlan(plan: WeekPlan) = plans.delete(plan)

    suspend fun createTask(task: Task): Long = tasks.insert(task.copy(title = task.title.trim()))

    suspend fun updateTask(task: Task) = tasks.update(task.copy(title = task.title.trim()))

    suspend fun deleteTask(task: Task) = tasks.delete(task)

    suspend fun assignPlanTo(planId: Long, date: LocalDate) {
        assignments.insert(
            WeekAssignment(
                planId = planId,
                isoYear = date.get(IsoFields.WEEK_BASED_YEAR),
                isoWeek = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            )
        )
    }
}
