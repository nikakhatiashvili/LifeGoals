package com.example.lifegoals.data.repository

import android.content.Context
import com.example.lifegoals.data.model.*
import com.example.lifegoals.dataSource.GoalLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val local: GoalLocalDataSource,
    private val timeProvider: TimeProvider
) {

    suspend fun toggleGoalCompletion(goal: GoalEntity) {
        val zone = ZoneId.systemDefault()
        val todayStart = timeProvider.today().atStartOfDay(zone).toEpochSecond() * 1000
        val tomorrowStart = timeProvider.today().plusDays(1).atStartOfDay(zone).toEpochSecond() * 1000

        when (goal.type) {
            GoalType.DAILY -> {
                val todayCompletions = local.getCompletionsBetweenOnce(todayStart, tomorrowStart)
                val isCompleted = todayCompletions.any { it.goalId == goal.id }

                if (isCompleted) {
                    local.deleteCompletionsInRange(goal.id, todayStart, tomorrowStart)
                } else {
                    local.insertHistory(
                        CompletionHistoryEntity(
                            goalId = goal.id,
                            completedAt = timeProvider.currentTimeMillis(),
                            pointsAwarded = goal.pointsReward
                        )
                    )
                }
            }

            GoalType.MILESTONE -> {
                val alreadyCompleted = local.hasCompletion(goal.id)
                if (alreadyCompleted) {
                    local.deleteAllCompletionsForGoal(goal.id)
                } else {
                    local.insertHistory(
                        CompletionHistoryEntity(
                            goalId = goal.id,
                            completedAt = timeProvider.currentTimeMillis(),
                            pointsAwarded = goal.pointsReward
                        )
                    )
                }
            }
        }
    }

    fun getGoalsWithCompletion(): Flow<List<GoalWithCompletion>> =
        combine(
            local.getAllGoals(),
            local.getAllHistory(),
            local.getCompletionsBetween(todayStart(), tomorrowStart())
        ) { goals, allHistory, todayCompletions ->

            val todayCompletedIds = todayCompletions.map { it.goalId }.toSet()
            val milestoneCompletedIds = allHistory.map { it.goalId }.toSet()

            goals.map { goal ->
                val isCompleted = when (goal.type) {
                    GoalType.DAILY -> todayCompletedIds.contains(goal.id)
                    GoalType.MILESTONE -> milestoneCompletedIds.contains(goal.id)
                }
                GoalWithCompletion(goal, isCompleted)
            }
        }

    private fun todayStart(): Long {
        val zone = ZoneId.systemDefault()
        return timeProvider.today().atStartOfDay(zone).toEpochSecond() * 1000
    }

    private fun tomorrowStart(): Long {
        val zone = ZoneId.systemDefault()
        return timeProvider.today().plusDays(1).atStartOfDay(zone).toEpochSecond() * 1000
    }
    fun getUiStats(): Flow<UiStats> =
        combine(local.getAllGoals(), local.getAllHistory()) { goals, history ->
            computeUiStats(goals, history)
        }

    suspend fun addGoal(goal: GoalEntity) = local.insertGoal(goal)

    private fun computeUiStats(
        goals: List<GoalEntity>,
        history: List<CompletionHistoryEntity>
    ): UiStats {
        val zone = ZoneId.systemDefault()
        val today = timeProvider.today()

        val todayStart = today.atStartOfDay(zone).toEpochSecond() * 1000
        val tomorrowStart = today.plusDays(1).atStartOfDay(zone).toEpochSecond() * 1000

        val pointsToday = history
            .filter { it.completedAt in todayStart until tomorrowStart }
            .sumOf { it.pointsAwarded }

        val totalPoints = history.sumOf { it.pointsAwarded }

        val dailyGoals = goals.filter { it.type == GoalType.DAILY }
        val completedTodayIds = history
            .filter { it.completedAt in todayStart until tomorrowStart }
            .map { it.goalId }
            .toSet()
        val goalsCompletedToday = dailyGoals.count { it.id in completedTodayIds }

        val datesWithCompletions = history
            .map { Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        val currentStreak = calculateCurrentStreak(datesWithCompletions, timeProvider.today())
        val bestStreak = calculateBestStreak(datesWithCompletions)

        // --- weekly completion rate (rolling 7-day window) ---
        val oneWeekAgo = today.minusDays(6)
        val totalPossible = dailyGoals.size * 7
        val totalCompleted = history.count {
            val date = Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate()
            it.goalId in dailyGoals.map { g -> g.id } && date >= oneWeekAgo
        }
        val weeklyCompletionRate =
            if (dailyGoals.isNotEmpty() && totalPossible > 0)
                totalCompleted.toFloat() / totalPossible
            else null

        val milestoneGoals = goals.filter { it.type == GoalType.MILESTONE }
        val completedMilestones = milestoneGoals.filter { goal ->
            history.any { it.goalId == goal.id }
        }

        val milestonesCompleted = completedMilestones.size
        val milestonesTotal = milestoneGoals.size
        val milestonePointsCompleted = completedMilestones.sumOf { it.pointsReward }
        val milestonePointsTotal = milestoneGoals.sumOf { it.pointsReward }

        return UiStats(
            pointsToday = pointsToday,
            totalPoints = totalPoints,
            goalsCompletedToday = goalsCompletedToday,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            weeklyCompletionRate = weeklyCompletionRate,
            milestonesCompleted = milestonesCompleted,
            milestonesTotal = milestonesTotal,
            milestonePointsCompleted = milestonePointsCompleted,
            milestonePointsTotal = milestonePointsTotal
        )
    }

    private fun calculateCurrentStreak(
        dates: List<LocalDate>,
        today: LocalDate
    ): Int {
        if (dates.isEmpty()) return 0

        val startDay = when {
            today in dates -> today
            today.minusDays(1) in dates -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        var day = startDay
        while (day in dates) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }
    private fun calculateBestStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        var best = 1
        var current = 1

        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                best = maxOf(best, current)
            } else {
                current = 1
            }
        }

        return best
    }


}


interface TimeProvider {
    fun currentTimeMillis(): Long
    fun today(): LocalDate
}

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis() = System.currentTimeMillis()
    override fun today(): LocalDate = LocalDate.now()
}