package com.example.lifegoals.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_history",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class CompletionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val completedAt: Long,
    val pointsAwarded: Int
)