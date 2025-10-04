package com.example.lifegoals.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.yourapp.widget.GoalListWidget

class GoalListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget =
        GoalListWidget()
}
