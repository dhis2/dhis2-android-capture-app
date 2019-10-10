package org.dhis2.usescases.login

import co.infinum.goldfinger.rx.RxGoldfinger
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
@PerActivity
class LoginModule(private val view: LoginContracts.View) {


    @Provides
    @PerActivity
    fun providePresenter(preferenceProvider: PreferenceProvider,
                         schedulerProvider : SchedulerProvider,
                         rxGoldfinger: RxGoldfinger): LoginPresenter {
        return LoginPresenter(view ,preferenceProvider,schedulerProvider, rxGoldfinger)
    }
}
