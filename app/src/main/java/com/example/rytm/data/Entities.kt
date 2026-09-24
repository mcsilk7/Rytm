package com.example.rytm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "week_plans")
data class WeekPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#1E7560",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = WeekPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val title: String,
    val note: String = "",
    val dayOfWeek: Int = 1,
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val colorHex: String = "#1E7560",
    val reminderMinutes: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "week_assignments",
    foreignKeys = [
        ForeignKey(
            entity = WeekPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("planId"),
        Index(value = ["isoYear", "isoWeek"], unique = true)
    ]
)
data class WeekAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val isoYear: Int,
    val isoWeek: Int
)
