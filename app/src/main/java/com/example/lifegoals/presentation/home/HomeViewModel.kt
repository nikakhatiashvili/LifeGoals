package com.example.lifegoals.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.data.model.GoalWithCompletion
import com.example.lifegoals.data.model.UiStats
import com.example.lifegoals.data.repository.GoalRepository
import com.example.lifegoals.presentation.home.actions.GoalActions
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    val allGoals: Flow<List<GoalWithCompletion>> = repository.getGoalsWithCompletion()

    val uiStats: Flow<UiStats> = repository.getUiStats()

    fun addGoal(goal: GoalEntity) {
        Log.d("asdasdas", "addGoal: called ${goal}")
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGoal(goal)
        }
    }

    fun handleAction(action: GoalActions) {
        when (action) {
            is GoalActions.OnCheckBoxClick -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.toggleGoalCompletion(action.goal.goal)
                }
            }

            is GoalActions.OnWholeItemClick -> {}
        }
    }
}
