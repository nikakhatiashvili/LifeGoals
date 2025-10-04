package com.example.lifegoals.widget

import android.content.Context
import androidx.datastore.core.DataStore
import com.example.lifegoals.data.model.GoalWithCompletion
import kotlinx.coroutines.flow.Flow

class GoalDataStore(private val context: Context) : DataStore<List<GoalWithCompletion>> {
    override val data: Flow<List<GoalWithCompletion>>
        get() {
            val repo = InjectedRepositoryProvider.repository
            return repo.getGoalsWithCompletion()
        }

    override suspend fun updateData(
        transform: suspend (t: List<GoalWithCompletion>) -> List<GoalWithCompletion>
    ): List<GoalWithCompletion> {
        throw NotImplementedError("GoalDataStore is read-only")
    }
}