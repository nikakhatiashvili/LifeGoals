package com.example.lifegoals.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.data.model.GoalType
import com.example.lifegoals.data.repository.GoalRepository
import com.example.lifegoals.data.repository.TimeProvider
import com.example.lifegoals.dataSource.GoalLocalDataSource
import com.example.lifegoals.dataSource.room.dao.CompletionHistoryDao
import com.example.lifegoals.dataSource.room.dao.GoalDao
import com.example.lifegoals.dataSource.room.database.AppDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Singleton
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var goalDao: GoalDao
    private lateinit var historyDao: CompletionHistoryDao
    private lateinit var dataSource: GoalLocalDataSource
    private lateinit var repository: GoalRepository
    private lateinit var timeProvider: FakeTimeProvider

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
        timeProvider = FakeTimeProvider(System.currentTimeMillis())
        repository = GoalRepository(dataSource, timeProvider)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun milestone_doesNotResetNextDay() = runTest {
        val milestone = GoalEntity(1, "Lose 5kg", "", 100, GoalType.MILESTONE)
        repository.addGoal(milestone)

        repository.toggleGoalCompletion(milestone)
        var goals = repository.getGoalsWithCompletion().first()
        assertTrue(goals.first().isCompleted)

        timeProvider.advanceByDays(10)

        goals = repository.getGoalsWithCompletion().first()
        assertTrue(goals.first().isCompleted)
    }

    @Test
    fun timeProvider_dayBoundaries_areCorrect() {
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val provider = FakeTimeProvider(now)

        val todayStart = provider.today().atStartOfDay(zone).toEpochSecond() * 1000
        val tomorrowStart = provider.today().plusDays(1).atStartOfDay(zone).toEpochSecond() * 1000

        println("Today start: ${Instant.ofEpochMilli(todayStart).atZone(zone)}")
        println("Tomorrow start: ${Instant.ofEpochMilli(tomorrowStart).atZone(zone)}")

        assertEquals(24 * 60 * 60 * 1000, tomorrowStart - todayStart)
    }

    @Test
    fun dailyGoals_resetNextDay_butMilestones_stayCompleted() = runTest {
        val daily1 = GoalEntity(1, "Daily1", "", 2, GoalType.DAILY)
        val daily2 = GoalEntity(2, "Daily2", "", 2, GoalType.DAILY)
        val milestone1 = GoalEntity(3, "Milestone1", "", 100, GoalType.MILESTONE)
        val milestone2 = GoalEntity(4, "Milestone2", "", 200, GoalType.MILESTONE)

        repository.addGoal(daily1)
        repository.addGoal(daily2)
        repository.addGoal(milestone1)
        repository.addGoal(milestone2)

        //  Complete today's tasks
        repository.toggleGoalCompletion(daily1, )
        repository.toggleGoalCompletion(daily2, )
        repository.toggleGoalCompletion(milestone1, )

        var goals = repository.getGoalsWithCompletion().first()

        // Verify: both daily and one milestone completed
        val dailyCompleted = goals.filter { it.goal.type == GoalType.DAILY && it.isCompleted }
        val milestoneCompleted = goals.filter { it.goal.type == GoalType.MILESTONE && it.isCompleted }

        assertEquals(2, dailyCompleted.size)
        assertEquals(1, milestoneCompleted.size)

        // Simulate 4 days passing
        timeProvider.advanceByDays(4)

        goals = repository.getGoalsWithCompletion().first()

        val dailyAfter = goals.filter { it.goal.type == GoalType.DAILY && it.isCompleted }
        val milestoneAfter = goals.filter { it.goal.type == GoalType.MILESTONE && it.isCompleted }

        // Verify: daily goals reset (0), milestones persist (1)
        assertEquals(0, dailyAfter.size)
        assertEquals(1, milestoneAfter.size)
    }


    @Test
    fun uncompletingMilestone_doesNotAffectStreakOrTodayPoints() = runTest {
        // --- Setup goals ---
        val daily1 = GoalEntity(1, "Daily1", "", 2, GoalType.DAILY)
        val daily2 = GoalEntity(2, "Daily2", "", 3, GoalType.DAILY)
        val milestone1 = GoalEntity(3, "Milestone1", "", 100, GoalType.MILESTONE)
        val milestone2 = GoalEntity(4, "Milestone2", "", 200, GoalType.MILESTONE)

        repository.addGoal(daily1)
        repository.addGoal(daily2)
        repository.addGoal(milestone1)
        repository.addGoal(milestone2)

        // --- Complete both dailies and 1 milestone yesterday ---
        repository.toggleGoalCompletion(daily1, )
        repository.toggleGoalCompletion(daily2, )
        repository.toggleGoalCompletion(milestone1, )

        // move one day forward (simulate "yesterday" happened)
        timeProvider.advanceByDays(1)

        // compute stats at start of today
        var statsBefore = repository.getUiStats().first()

        // sanity: yesterday’s completions give us streak = 1
        assertEquals(1, statsBefore.currentStreak)
        val totalPointsBefore = statsBefore.totalPoints
        val pointsTodayBefore = statsBefore.pointsToday

        // --- Uncomplete the milestone today ---
        repository.toggleGoalCompletion(milestone1, ) // remove milestone completion

        val statsAfter = repository.getUiStats().first()

        // ✅ Expectations:
        // - total points decreased by the milestone’s reward
        // - pointsToday did NOT increase (since we uncompleted)
        // - current streak did NOT increase
        // - goalsCompletedToday unchanged (still 0 because we didn’t complete a daily today)
        assertEquals(totalPointsBefore - milestone1.pointsReward, statsAfter.totalPoints)
        assertEquals(pointsTodayBefore, statsAfter.pointsToday)
        assertEquals(1, statsAfter.currentStreak)
        assertEquals(0, statsAfter.goalsCompletedToday)
    }

    @Test
    fun pointsToday_resetsAfterNextDay() = runTest {
        // --- Setup ---
        val daily1 = GoalEntity(1, "Daily1", "", 2, GoalType.DAILY)
        val daily2 = GoalEntity(2, "Daily2", "", 3, GoalType.DAILY)
        repository.addGoal(daily1)
        repository.addGoal(daily2)

        // --- Complete both dailies today ---
        repository.toggleGoalCompletion(daily1, )
        repository.toggleGoalCompletion(daily2, )

        // Verify current day points
        var statsToday = repository.getUiStats().first()
        assertEquals(5, statsToday.pointsToday) // 2 + 3
        val totalPointsBefore = statsToday.totalPoints

        // --- Simulate next day ---
        timeProvider.advanceByDays(1)

        // Get stats for new day
        val statsTomorrow = repository.getUiStats().first()

        // ✅ Expectations:
        // - Points today should reset to 0 (new day)
        // - Total points should remain cumulative
        assertEquals(0, statsTomorrow.pointsToday)
        assertEquals(totalPointsBefore, statsTomorrow.totalPoints)
    }

    @Test
    fun milestoneStats_updateCorrectly_afterUncompletion() = runTest {
        // --- Setup milestones ---
        val m1 = GoalEntity(1, "Milestone1", "", 100, GoalType.MILESTONE)
        val m2 = GoalEntity(2, "Milestone2", "", 150, GoalType.MILESTONE)
        val m3 = GoalEntity(3, "Milestone3", "", 200, GoalType.MILESTONE)
        val m4 = GoalEntity(4, "Milestone4", "", 50, GoalType.MILESTONE)

        repository.addGoal(m1)
        repository.addGoal(m2)
        repository.addGoal(m3)
        repository.addGoal(m4)

        // --- Complete two milestones (m1, m2) ---
        repository.toggleGoalCompletion(m1, )
        repository.toggleGoalCompletion(m2, )

        // --- Verify stats after completion ---
        var stats = repository.getUiStats().first()
        assertEquals(2, stats.milestonesCompleted)
        assertEquals(4, stats.milestonesTotal)
        assertEquals(250, stats.milestonePointsCompleted) // 100 + 150
        assertEquals(500, stats.milestonePointsTotal)     // 100 + 150 + 200 + 50

        val totalPointsBefore = stats.totalPoints

        // --- Uncomplete one milestone (m1) ---
        repository.toggleGoalCompletion(m1, )

        // --- Verify stats after uncompletion ---
        stats = repository.getUiStats().first()

        // ✅ Expectations:
        // - milestonesCompleted decreased by 1 (from 2 → 1)
        // - milestonePointsCompleted decreased by 100 (from 250 → 150)
        // - milestonesTotal and milestonePointsTotal remain the same
        // - totalPoints decreased appropriately
        assertEquals(1, stats.milestonesCompleted)
        assertEquals(4, stats.milestonesTotal)
        assertEquals(150, stats.milestonePointsCompleted)
        assertEquals(500, stats.milestonePointsTotal)
        assertEquals(totalPointsBefore - 100, stats.totalPoints)
    }
    @Test
    fun weeklyCompletionRate_reflectsSevenDayCompletionPercentage() = runTest {
        // --- Setup ---
        val g1 = GoalEntity(1, "Daily1", "", 2, GoalType.DAILY)
        val g2 = GoalEntity(2, "Daily2", "", 3, GoalType.DAILY)
        repository.addGoal(g1)
        repository.addGoal(g2)

        // Helper: complete all daily goals for a day
        suspend fun completeAllDailies() {
            repository.toggleGoalCompletion(g1, )
            repository.toggleGoalCompletion(g2, )
        }

        // Helper: complete only one daily goal
        suspend fun completeHalfDailies() {
            repository.toggleGoalCompletion(g1, )
        }

        // --- Simulate a perfect 7-day streak ---
        // --- Simulate a perfect 7-day streak ---
        repeat(7) {
            completeAllDailies()
            if (it < 6) timeProvider.advanceByDays(1) // advance only after day 1–6
        }


        var stats = repository.getUiStats().first()

        // ✅ Expect 100% completion (1.0f)
        assertNotNull(stats.weeklyCompletionRate)
        assertEquals(1.0f, stats.weeklyCompletionRate!!, 0.0001f)

        // --- Now simulate a partial week: only 1 goal completed each day ---
        val startOfNewWeek = timeProvider.today()
        // --- Simulate a perfect 7-day streak ---
        repeat(7) {
            completeHalfDailies()
            if (it < 6) timeProvider.advanceByDays(1) // advance only after day 1–6
        }

        stats = repository.getUiStats().first()

        // ✅ Expect 50% completion rate
        // In this 7-day window, we had 14 possible completions (2 goals × 7 days)
        // but completed 7 total (1 goal per day)
        val expectedRate = 7f / 14f
        assertNotNull(stats.weeklyCompletionRate)
        assertEquals(expectedRate, stats.weeklyCompletionRate!!, 0.0001f)
    }


}



@Singleton
class FakeTimeProvider(
    private var currentTimeMillis: Long
) : TimeProvider {

    override fun currentTimeMillis(): Long = currentTimeMillis

    override fun today(): LocalDate =
        LocalDate.ofEpochDay(currentTimeMillis / (24 * 60 * 60 * 1000))

    fun advanceByDays(days: Long) {
        currentTimeMillis += days * 24 * 60 * 60 * 1000
    }

    fun setTimeMillis(timeMillis: Long) {
        currentTimeMillis = timeMillis
    }
}
