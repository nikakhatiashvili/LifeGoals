package com.example.lifegoals.data.model

data class UiStats(
    val pointsToday: Int,             // total points earned today (from DAILY + milestones completed today)
    val totalPoints: Int,             // total points across all history
    val goalsCompletedToday: Int,     // count of unique DAILY goals completed today
    val currentStreak: Int,           // consecutive days with at least 1 goal completed
    val bestStreak: Int,              // max consecutive days with at least 1 goal completed
    val weeklyCompletionRate: Float?, // percentage of DAILY goals completed in last 7 days
    val milestonesCompleted: Int,     // number of milestones completed all time
    val milestonesTotal: Int,         // total milestones
    val milestonePointsCompleted: Int,// sum of milestone points earned
    val milestonePointsTotal: Int     // total milestone points available
)
