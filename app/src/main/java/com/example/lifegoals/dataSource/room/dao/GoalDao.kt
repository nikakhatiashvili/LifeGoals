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

    // ✅ Explicit range instead of date('now')
    @Query("SELECT * FROM completion_history WHERE completedAt BETWEEN :start AND :end")
    fun getCompletionsBetween(start: Long, end: Long): Flow<List<CompletionHistoryEntity>>

    // ✅ One-shot version for suspend functions
    @Query("SELECT * FROM completion_history WHERE completedAt BETWEEN :start AND :end")
    suspend fun getCompletionsBetweenOnce(start: Long, end: Long): List<CompletionHistoryEntity>

    @Query("DELETE FROM completion_history WHERE goalId = :goalId AND completedAt BETWEEN :start AND :end")
    suspend fun deleteCompletionsInRange(goalId: Int, start: Long, end: Long)

    @Query("SELECT * FROM completion_history ORDER BY completedAt ASC")
    fun getAllHistory(): Flow<List<CompletionHistoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM completion_history WHERE goalId = :goalId)")
    suspend fun hasCompletion(goalId: Int): Boolean

    @Query("DELETE FROM completion_history WHERE goalId = :goalId")
    suspend fun deleteAllCompletionsForGoal(goalId: Int)
}


