package org.dhis2.utils.session

import dagger.Module
import dagger.Provides
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2

@Module
class ChangeServerURLModule(val view: ChangeServerURLView) { @Provides
    fun providesPresenter(
    d2: D2?,
    preferenceProvider: PreferenceProvider,
    schedulerProvider: SchedulerProvider,
    ): ChangeServerURLPresenter {
        return ChangeServerURLPresenter(view, preferenceProvider, schedulerProvider, d2)
    }
}
