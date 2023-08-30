package org.dhis2.usescases.about

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.user.UserRepository
import org.hisp.dhis.android.core.D2

@Module
class AboutModule(val view: AboutView) {
    @Provides
    @PerFragment
    fun providesPresenter(
        d2: D2,
        provider: SchedulerProvider,
        userRepository: UserRepository,
    ): AboutPresenter {
        return AboutPresenter(view, d2, provider, userRepository)
    }
}
