package org.dhis2.usescases.main

import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.D2

@Module
class MainModule(val view: MainView) {

    @Provides
    @PerActivity
    fun homePresenter(
        d2: D2,
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
        workManager: WorkManager,
        filterManager: FilterManager
    ): MainPresenter {
        return MainPresenter(view, d2, schedulerProvider, preferences, workManager, filterManager)
    }
}
