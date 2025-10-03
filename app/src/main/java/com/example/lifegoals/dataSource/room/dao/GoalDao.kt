package com.example.lifegoals.dataSource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lifegoals.data.model.CompletionHistoryEntity
import com.example.lifegoals.data.model.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>
}

@Dao
interface CompletionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CompletionHistoryEntity)

    @Query("SELECT * FROM completion_history WHERE goalId = :goalId ORDER BY completedAt DESC")
    fun getHistoryForGoal(goalId: Int): Flow<List<CompletionHistoryEntity>>

    @Query("SELECT * FROM completion_history WHERE date(completedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun getTodayCompletions(): Flow<List<CompletionHistoryEntity>>

    @Query("DELETE FROM completion_history WHERE goalId = :goalId AND date(completedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun deleteTodayCompletion(goalId: Int)

    @Query("SELECT * FROM completion_history ORDER BY completedAt ASC")
    fun getAllHistory(): Flow<List<CompletionHistoryEntity>>

    @Query("SELECT * FROM completion_history WHERE completedAt BETWEEN :start AND :end")
    fun getHistoryBetween(start: Long, end: Long): Flow<List<CompletionHistoryEntity>>

    @Query("SELECT * FROM completion_history WHERE goalId = :goalId AND date(completedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun getTodayCompletionForGoal(goalId: Int): List<CompletionHistoryEntity>

    @Query("SELECT * FROM completion_history WHERE date(completedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun getTodayCompletionsOnce(): List<CompletionHistoryEntity>}