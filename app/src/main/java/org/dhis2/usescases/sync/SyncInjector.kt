package org.dhis2.usescases.sync

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.hisp.dhis.android.core.D2

@PerActivity
@Subcomponent(modules = [SyncModule::class])
interface SyncComponent {
    fun inject(syncActivity: SyncActivity)
}

@Module
@PerActivity
class SyncModule(private val view: SyncView) {
    @Provides
    @PerActivity
    fun providePresenter(
        syncRepository: SyncRepository,
        schedulerProvider: SchedulerProvider,
        workManagerController: WorkManagerController,
        preferences: PreferenceProvider
    ): SyncPresenter {
        return SyncPresenter(
            view,
            syncRepository,
            schedulerProvider,
            workManagerController,
            preferences
        )
    }

    @Provides
    @PerActivity
    fun providesRepository(
        d2: D2,
        systemStyleMapper: SystemStyleMapper
    ): SyncRepository {
        return SyncRepository(
            d2,
            systemStyleMapper
        )
    }
}
