package org.dhis2.usescases.login

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
@PerActivity
object LoginModule {

    @JvmStatic
    @Provides
    @PerActivity
    fun providePresenter(preferenceProvider: PreferenceProvider, schedulerProvider : SchedulerProvider): LoginPresenter {
        return LoginPresenter(preferenceProvider,schedulerProvider)
    }
}
