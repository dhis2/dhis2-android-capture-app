package org.dhis2.usescases.splash

import dagger.Module
import dagger.Provides
import javax.inject.Named
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.splash.SplashActivity.Companion.FLAG

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
class SplashModule internal constructor(
    private val splashView: SplashView,
    serverComponent: ServerComponent?
) {

    private val userManager: UserManager? = serverComponent?.userManager()

    @Provides
    @PerActivity
    fun providePresenter(
        schedulerProvider: SchedulerProvider,
        preferenceProvider: PreferenceProvider,
        crashReportController: CrashReportController
    ): SplashPresenter {
        return SplashPresenter(
            splashView,
            userManager,
            schedulerProvider,
            preferenceProvider,
            crashReportController
        )
    }

    @Provides
    @PerActivity
    @Named(FLAG)
    fun provideFlag(): String {
        return if (userManager?.d2 != null && userManager.isUserLoggedIn.blockingFirst()) {
            val systemSetting =
                userManager.d2.systemSettingModule().systemSetting().flag().blockingGet()
            if (systemSetting != null) {
                systemSetting.value() ?: ""
            } else {
                ""
            }
        } else {
            ""
        }
    }
}
