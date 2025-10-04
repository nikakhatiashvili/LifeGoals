package com.example.lifegoals.dataSource

import com.example.lifegoals.data.model.CompletionHistoryEntity
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.dataSource.room.dao.CompletionHistoryDao
import com.example.lifegoals.dataSource.room.dao.GoalDao
import javax.inject.Inject

class GoalLocalDataSource @Inject constructor(
    private val goalDao: GoalDao,
    private val historyDao: CompletionHistoryDao
) {
    suspend fun insertGoal(goal: GoalEntity) = goalDao.insertGoal(goal)
    fun getAllGoals() = goalDao.getAllGoals()
    fun getAllHistory() = historyDao.getAllHistory()

    suspend fun getCompletionsBetweenOnce(start: Long, end: Long) =
        historyDao.getCompletionsBetweenOnce(start, end)

    fun getCompletionsBetween(start: Long, end: Long) =
        historyDao.getCompletionsBetween(start, end)

    suspend fun deleteCompletionsInRange(goalId: Int, start: Long, end: Long) =
        historyDao.deleteCompletionsInRange(goalId, start, end)

    suspend fun insertHistory(history: CompletionHistoryEntity) =
        historyDao.insertHistory(history)

    suspend fun deleteAllCompletionsForGoal(goalId: Int) =
        historyDao.deleteAllCompletionsForGoal(goalId)

    suspend fun hasCompletion(goalId: Int) =
        historyDao.hasCompletion(goalId)
}
