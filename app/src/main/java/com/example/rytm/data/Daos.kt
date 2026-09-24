package com.example.rytm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekPlanDao {
    @Query("SELECT * FROM week_plans ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<WeekPlan>>

    @Query("SELECT * FROM week_plans ORDER BY createdAt ASC")
    suspend fun getAll(): List<WeekPlan>

    @Query("SELECT * FROM week_plans WHERE id = :id")
    suspend fun getById(id: Long): WeekPlan?

    @Insert
    suspend fun insert(plan: WeekPlan): Long

    @Update
    suspend fun update(plan: WeekPlan)

    @Delete
    suspend fun delete(plan: WeekPlan)

    @Query("SELECT COUNT(*) FROM week_plans")
    suspend fun count(): Int
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE planId = :planId ORDER BY dayOfWeek, startTime")
    fun observeForPlan(planId: Long): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}

@Dao
interface WeekAssignmentDao {
    @Query("SELECT * FROM week_assignments WHERE isoYear = :isoYear AND isoWeek = :isoWeek LIMIT 1")
    fun observeForWeek(isoYear: Int, isoWeek: Int): Flow<WeekAssignment?>

    @Query("SELECT * FROM week_assignments WHERE isoYear = :isoYear AND isoWeek = :isoWeek LIMIT 1")
    suspend fun getForWeek(isoYear: Int, isoWeek: Int): WeekAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: WeekAssignment): Long

    @Query("DELETE FROM week_assignments WHERE isoYear = :isoYear AND isoWeek = :isoWeek")
    suspend fun clearWeek(isoYear: Int, isoWeek: Int)
}
