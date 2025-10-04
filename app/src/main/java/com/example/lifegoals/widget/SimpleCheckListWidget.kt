package com.example.yourapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import com.example.lifegoals.data.model.GoalWithCompletion
import com.example.lifegoals.data.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.updateAll
import com.example.lifegoals.widget.InjectedRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Glance-based widget showing a list of goals with toggle checkboxes.
 */


import androidx.compose.ui.unit.sp

import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.TextStyle
import com.example.lifegoals.R
import com.example.lifegoals.widget.GoalStateDefinition

class GoalListWidget(

) : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition = GoalStateDefinition


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val goals = currentState<List<GoalWithCompletion>>()
            GoalListContent(goals)
        }
    }
    @Composable
    private fun GoalListContent(goals: List<GoalWithCompletion>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(R.color.white)
                .padding(12.dp)
        ) {
            goals.forEach { goal ->
                GoalRow(goal)
            }
        }
    }

    @Composable
    private fun GoalRow(goal: GoalWithCompletion) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = goal.goal.name,
                style = TextStyle(
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(GlanceModifier.width(12.dp))

            // Right: Checkbox (clickable)
            Text(
                text = if (goal.isCompleted) "☑️" else "⬜",
                style = TextStyle(fontSize = 20.sp),
                modifier = GlanceModifier
                    .clickable(
                        actionRunCallback<ToggleGoalAction>(
                            actionParametersOf(
                                ActionParameters.Key<Int>("goalId") to goal.goal.id
                            )
                        )
                    )
                    .padding(4.dp)
            )
        }
    }
}

class ToggleGoalAction : androidx.glance.appwidget.action.ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val goalId = parameters[ActionParameters.Key<Int>("goalId")] ?: return
        val repo = InjectedRepositoryProvider.repository

        CoroutineScope(Dispatchers.IO).launch {
            val goal = repo.getGoalsWithCompletion().first()
                .firstOrNull { it.goal.id == goalId }?.goal ?: return@launch
            repo.toggleGoalCompletion(goal)
            GoalListWidget().updateAll(context)
        }
    }
}
