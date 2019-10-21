package org.dhis2.usescases.splash

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.splash.SplashActivity.Companion.FLAG
import javax.inject.Named

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
class SplashModule internal constructor(private val splashView: SplashView, serverComponent: ServerComponent?) {

    private val userManager: UserManager? = serverComponent?.userManager()

    @Provides
    @PerActivity
    fun providePresenter(schedulerProvider: SchedulerProvider, preferenceProvider: PreferenceProvider): SplashPresenter {
        return SplashPresenter(splashView, userManager, schedulerProvider, preferenceProvider)
    }

    @Provides
    @PerActivity
    @Named(FLAG)
    fun provideFlag(): String {
        return if (userManager?.d2 != null) {
            val systemSetting = userManager.d2.systemSettingModule().systemSetting.flag().blockingGet()
            if (systemSetting != null)
                systemSetting.value() ?: ""
            else
                ""
        } else
            ""
    }

}
