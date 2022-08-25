package org.dhis2.android.rtsm.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dhis2.android.rtsm.data.persistence.AppDatabase
import org.dhis2.android.rtsm.data.persistence.UserActivityDao
import org.dhis2.android.rtsm.data.persistence.UserActivityRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext appContext: Context) = AppDatabase.getInstance(appContext)

    @Provides
    fun providesUserActivityDao(appDatabase: AppDatabase): UserActivityDao {
        return appDatabase.userActivityDao()
    }

    @Provides
    fun providesUserActivityRepository(userActivityDao: UserActivityDao): UserActivityRepository {
        return UserActivityRepository(userActivityDao)
    }
}