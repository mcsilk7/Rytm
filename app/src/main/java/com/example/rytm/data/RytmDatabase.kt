package com.example.rytm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WeekPlan::class, Task::class, WeekAssignment::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RytmDatabase : RoomDatabase() {
    abstract fun weekPlanDao(): WeekPlanDao
    abstract fun taskDao(): TaskDao
    abstract fun weekAssignmentDao(): WeekAssignmentDao

    companion object {
        @Volatile
        private var instance: RytmDatabase? = null

        fun getInstance(context: Context): RytmDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RytmDatabase::class.java,
                    "rytm.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { instance = it }
            }
    }
}
