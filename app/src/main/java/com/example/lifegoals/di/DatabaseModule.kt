package com.example.lifegoals.di

import android.content.Context
import androidx.room.Room
import com.example.lifegoals.dataSource.room.dao.CompletionHistoryDao
import com.example.lifegoals.dataSource.room.dao.GoalDao
import com.example.lifegoals.dataSource.room.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "life_goals_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideCompletionHistoryDao(db: AppDatabase): CompletionHistoryDao =
        db.historyDao()
}
