package org.dhis2.commons.service

import dagger.Module
import dagger.Provides
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Singleton

@Module
open class SessionManagerModule() {

    @Provides
    @Singleton
    fun providesSessionManagerService(
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
    ): SessionManagerService {
        return SessionManagerServiceImpl(provideSdk(), schedulerProvider, preferences)
    }

    @Provides
    @Singleton
    fun providesSessionManagerServiceImpl(
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
    ): SessionManagerServiceImpl {
        return SessionManagerServiceImpl(provideSdk(), schedulerProvider, preferences)
    }

    private fun provideSdk(): D2 {
        return D2Manager.getD2()
    }
}
