package com.example.lifegoals.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.lifegoals.data.repository.GoalRepository
import com.example.yourapp.widget.GoalListWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object WidgetUpdater {

    fun observeAndUpdateWidgets(context: Context, repository: GoalRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getGoalsWithCompletion().collectLatest {
                GoalListWidget().updateAll(context)
            }
        }
    }
}