package org.dhis2.usescases.main

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2

@Module
class MainModule(val view: MainView) {

    @Provides
    @PerActivity
    fun homePresenter(
        context: Context,
        d2: D2,
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider
    ): MainPresenter {
        val workManager = WorkManager.getInstance(context)

        return MainPresenter(view, d2, schedulerProvider, preferences, workManager)
    }
}
