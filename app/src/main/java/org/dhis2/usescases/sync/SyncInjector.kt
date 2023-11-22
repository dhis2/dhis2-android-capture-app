package org.dhis2.usescases.sync

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.service.workManager.WorkManagerController

@PerActivity
@Subcomponent(modules = [SyncModule::class])
interface SyncComponent {
    fun inject(syncActivity: SyncActivity)
}

@Module
class SyncModule(private val view: SyncView, serverComponent: ServerComponent?) {

    private val userManager = serverComponent?.userManager()

    @Provides
    @PerActivity
    fun providePresenter(
        schedulerProvider: SchedulerProvider,
        workManagerController: WorkManagerController,
        preferences: PreferenceProvider,
    ): SyncPresenter {
        return SyncPresenter(
            view,
            userManager,
            schedulerProvider,
            workManagerController,
            preferences,
        )
    }
}
