package com.example.lifegoals.data.model

data class UiStats(
    val pointsToday: Int,
    val totalPoints: Int,
    val goalsCompletedToday: Int,
    val totalGoalsToday: Int,
    val allGoalsCompleted: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val weeklyCompletionRate: Float?,
    val milestonesLeft: Int,
    val milestonePointsLeft: Int
)