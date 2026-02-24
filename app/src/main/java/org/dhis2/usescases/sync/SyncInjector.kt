package org.dhis2.usescases.sync

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.ServerComponent
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction

@PerActivity
@Subcomponent(modules = [SyncModule::class])
interface SyncComponent {
    fun inject(syncActivity: SyncActivity)
}

@Module
class SyncModule(
    private val view: SyncView,
    private val backgroundJobAction: SyncBackgroundJobAction,
    serverComponent: ServerComponent?,
) {
    private val userManager = serverComponent?.userManager()

    @Provides
    @PerActivity
    fun providePresenter(
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
    ): SyncPresenter =
        SyncPresenter(
            view,
            userManager,
            schedulerProvider,
            backgroundJobAction,
            preferences,
        )
}
