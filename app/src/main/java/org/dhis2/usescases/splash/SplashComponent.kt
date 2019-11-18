package org.dhis2.usescases.splash

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [SplashModule::class])
interface SplashComponent {
    fun inject(splashActivity: SplashActivity)
}
