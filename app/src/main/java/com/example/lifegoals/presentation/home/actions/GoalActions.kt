package com.example.lifegoals.presentation.home.actions

import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.data.model.GoalWithCompletion

sealed interface GoalActions {
    data class OnCheckBoxClick(val goal: GoalWithCompletion, val isChecked: Boolean) : GoalActions
    data class OnWholeItemClick(val goal: GoalWithCompletion) : GoalActions
}
