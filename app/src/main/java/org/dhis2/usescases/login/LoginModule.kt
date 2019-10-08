package org.dhis2.usescases.login

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
@PerActivity
class LoginModule {

    @Provides
    @PerActivity
    internal fun providePresenter(preferenceProvider: PreferenceProvider): LoginContracts.Presenter {
        return LoginPresenter(preferenceProvider)
    }


}
