package com.example.lifegoals.data.repository

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
    private val local: GoalLocalDataSource
) {

    suspend fun addGoal(goal: GoalEntity) = local.insertGoal(goal)

    suspend fun toggleGoalCompletion(goal: GoalEntity) {
        val todayCompletions = local.getTodayCompletionsOnce()
        val isCompleted = todayCompletions.any { it.goalId == goal.id }

        if (isCompleted) {
            local.deleteTodayCompletion(goal.id)
        } else {
            val history = CompletionHistoryEntity(
                goalId = goal.id,
                completedAt = System.currentTimeMillis(),
                pointsAwarded = goal.pointsReward
            )
            local.insertHistory(history)
        }
    }

    fun getGoalsWithCompletion(): Flow<List<GoalWithCompletion>> =
        combine(local.getAllGoals(), local.getTodayCompletions()) { goals, completions ->
            val todayCompletedIds = completions.map { it.goalId }.toSet()

            goals.map { goal ->
                val isCompleted = when (goal.type) {
                    GoalType.DAILY -> todayCompletedIds.contains(goal.id)
                    GoalType.MILESTONE -> completions.any { it.goalId == goal.id }
                }
                GoalWithCompletion(goal, isCompleted)
            }
        }

    fun getUiStats(): Flow<UiStats> =
        combine(local.getAllGoals(), local.getAllHistory()) { goals, history ->
            computeUiStats(goals, history)
        }

    private fun computeUiStats(
        goals: List<GoalEntity>,
        history: List<CompletionHistoryEntity>
    ): UiStats {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        val tomorrowStart =
            LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000

        val pointsToday = history.filter { it.completedAt in todayStart until tomorrowStart }
            .sumOf { it.pointsAwarded }
        val totalPoints = history.sumOf { it.pointsAwarded }

        val dailyGoals = goals.filter { it.type == GoalType.DAILY }
        val completedTodayIds = history
            .filter { it.completedAt in todayStart until tomorrowStart }
            .map { it.goalId }
            .toSet()
        val goalsCompletedToday = dailyGoals.count { it.id in completedTodayIds }
        val allGoalsCompleted = goalsCompletedToday == dailyGoals.size && dailyGoals.isNotEmpty()

        val datesWithCompletions = history
            .map {
                Instant.ofEpochMilli(it.completedAt).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .distinct()
            .sorted()

        val currentStreak = calculateCurrentStreak(datesWithCompletions)
        val bestStreak = calculateBestStreak(datesWithCompletions)

        val oneWeekAgo = LocalDate.now().minusDays(6)
        val weeklyCompletionRate =
            if (datesWithCompletions.size >= 7) {
                val totalPossible = dailyGoals.size * 7
                val totalCompleted = history.count {
                    val date = Instant.ofEpochMilli(it.completedAt)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    it.goalId in dailyGoals.map { g -> g.id } && date >= oneWeekAgo
                }
                if (totalPossible > 0) totalCompleted.toFloat() / totalPossible else 0f
            } else null

        val milestoneGoals = goals.filter { it.type == GoalType.MILESTONE }
        val completedMilestones = milestoneGoals.filter { goal ->
            history.any { it.goalId == goal.id }
        }
        val milestonesLeft = milestoneGoals.size - completedMilestones.size
        val milestonePointsLeft = milestoneGoals.filterNot { it in completedMilestones }
            .sumOf { it.pointsReward }

        return UiStats(
            pointsToday = pointsToday,
            totalPoints = totalPoints,
            goalsCompletedToday = goalsCompletedToday,
            totalGoalsToday = dailyGoals.size,
            allGoalsCompleted = allGoalsCompleted,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            weeklyCompletionRate = weeklyCompletionRate,
            milestonesLeft = milestonesLeft,
            milestonePointsLeft = milestonePointsLeft
        )
    }

    private fun calculateCurrentStreak(dates: List<LocalDate>): Int {
        var streak = 0
        var day = LocalDate.now()
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
