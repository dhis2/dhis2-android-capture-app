package org.dhis2.usescases.splash

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.UserManager
import org.dhis2.data.sharedPreferences.SharePreferencesProvider
import org.dhis2.usescases.splash.SplashActivity.Companion.FLAG
import javax.inject.Named

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
class SplashModule internal constructor(serverComponent: ServerComponent?) {

    private val userManager: UserManager? = serverComponent?.userManager()

    @Provides
    @PerActivity
    fun providePresenter(provider: SharePreferencesProvider): SplashContracts.Presenter {
        return SplashPresenter(userManager, provider)
    }

    @Provides
    @PerActivity
    @Named(FLAG)
    fun provideFlag(): String {
        return if (userManager?.d2 != null) {
            val systemSetting = userManager.d2.systemSettingModule().systemSetting.flag().get()
            if (systemSetting != null)
                systemSetting.value() ?: ""
            else
                ""
        } else
            ""
    }

}
