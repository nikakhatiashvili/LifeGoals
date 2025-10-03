package com.example.lifegoals.dataSource

import com.example.lifegoals.data.model.CompletionHistoryEntity
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.dataSource.room.dao.CompletionHistoryDao
import com.example.lifegoals.dataSource.room.dao.GoalDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalLocalDataSource @Inject constructor(
    private val goalDao: GoalDao,
    private val historyDao: CompletionHistoryDao
) {

    suspend fun insertGoal(goal: GoalEntity) = goalDao.insertGoal(goal)
    fun getAllGoals(): Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun insertHistory(history: CompletionHistoryEntity) = historyDao.insertHistory(history)
    suspend fun deleteTodayCompletion(goalId: Int) = historyDao.deleteTodayCompletion(goalId)

    fun getTodayCompletions(): Flow<List<CompletionHistoryEntity>> = historyDao.getTodayCompletions()
    suspend fun getTodayCompletionsOnce(): List<CompletionHistoryEntity> = historyDao.getTodayCompletionsOnce()
    fun getAllHistory(): Flow<List<CompletionHistoryEntity>> = historyDao.getAllHistory()
}