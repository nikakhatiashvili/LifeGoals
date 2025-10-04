package com.example.lifegoals.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import com.example.lifegoals.data.model.GoalWithCompletion
import java.io.File

object GoalStateDefinition : GlanceStateDefinition<List<GoalWithCompletion>> {
    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<List<GoalWithCompletion>> {
        return GoalDataStore(context)
    }

    override fun getLocation(context: Context, fileKey: String): File {
        throw NotImplementedError("Not needed for in-memory or DB-backed store")
    }
}