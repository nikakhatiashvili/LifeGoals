package com.example.lifegoals

import android.app.Application
import com.example.lifegoals.data.repository.GoalRepository
import com.example.lifegoals.widget.InjectedRepositoryProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LifeGoalsApp: Application() {
    @Inject
    lateinit var repo: GoalRepository

    override fun onCreate() {
        super.onCreate()
        InjectedRepositoryProvider.repository = repo
    }
}