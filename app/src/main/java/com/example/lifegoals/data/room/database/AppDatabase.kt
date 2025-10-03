package com.example.lifegoals.data.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lifegoals.data.model.CompletionHistoryEntity
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.data.room.dao.CompletionHistoryDao
import com.example.lifegoals.data.room.dao.GoalDao

@Database(
    entities = [GoalEntity::class, CompletionHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun historyDao(): CompletionHistoryDao
}