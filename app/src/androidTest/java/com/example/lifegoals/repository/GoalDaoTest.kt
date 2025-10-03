package com.example.lifegoals.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.data.model.GoalType
import com.example.lifegoals.data.repository.GoalRepository
import com.example.lifegoals.dataSource.GoalLocalDataSource
import com.example.lifegoals.dataSource.room.dao.CompletionHistoryDao
import com.example.lifegoals.dataSource.room.dao.GoalDao
import com.example.lifegoals.dataSource.room.database.AppDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var goalDao: GoalDao
    private lateinit var historyDao: CompletionHistoryDao
    private lateinit var dataSource: GoalLocalDataSource
    private lateinit var repository: GoalRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        goalDao = db.goalDao()
        historyDao = db.historyDao()

        dataSource = GoalLocalDataSource(goalDao, historyDao)

        repository = GoalRepository(dataSource)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertGoal_andFetchAllGoals_returnsInsertedGoal() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            name = "Lose Weight",
            description = "500 calorie deficit",
            pointsReward = 2,
            type = GoalType.DAILY
        )

        goalDao.insertGoal(goal)

        val result = goalDao.getAllGoals().first()
        assertEquals(1, result.size)
        assertEquals(goal, result[0])
    }

    @Test
    fun toggleGoalCompletion_marksAndUnmarksGoal() = runTest {
        val goal = GoalEntity(
            id = 1,
            name = "Test Goal",
            description = "Desc",
            pointsReward = 5,
            type = GoalType.DAILY
        )
        repository.addGoal(goal)

        // Initially no completions
        assertTrue(historyDao.getTodayCompletionsOnce().isEmpty())

        // First toggle → should insert completion
        repository.toggleGoalCompletion(goal)
        var completions = historyDao.getTodayCompletionsOnce()
        assertEquals(1, completions.size)
        assertEquals(goal.id, completions[0].goalId)

        // Second toggle → should remove completion
        repository.toggleGoalCompletion(goal)
        completions = historyDao.getTodayCompletionsOnce()
        assertTrue(completions.isEmpty())
    }

    @Test
    fun getGoalsWithCompletion_reflectsCompletedGoals() = runTest {
        val goal1 = GoalEntity(1, "Goal 1", "Desc", 2, GoalType.DAILY)
        val goal2 = GoalEntity(2, "Goal 2", "Desc", 3, GoalType.DAILY)
        repository.addGoal(goal1)
        repository.addGoal(goal2)

        // Initially none completed
        var goalsWithCompletion = repository.getGoalsWithCompletion().first()
        assertFalse(goalsWithCompletion[0].isCompleted)
        assertFalse(goalsWithCompletion[1].isCompleted)

        // Mark goal1 completed
        repository.toggleGoalCompletion(goal1)
        goalsWithCompletion = repository.getGoalsWithCompletion().first()
        val completedGoal = goalsWithCompletion.find { it.goal.id == goal1.id }
        val notCompletedGoal = goalsWithCompletion.find { it.goal.id == goal2.id }

        assertTrue(completedGoal?.isCompleted == true)
        assertFalse(notCompletedGoal?.isCompleted == true)
    }


}
