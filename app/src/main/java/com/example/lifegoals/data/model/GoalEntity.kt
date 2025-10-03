package com.example.lifegoals.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String? = null,
    val pointsReward: Int,
    val type: GoalType
)

enum class GoalType {
    DAILY,
    MILESTONE
}