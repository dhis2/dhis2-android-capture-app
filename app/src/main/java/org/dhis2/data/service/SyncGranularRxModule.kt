package org.dhis2.data.service

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerService
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.hisp.dhis.android.core.D2

@Module
class SyncGranularRxModule(
    private val syncStatusController: SyncStatusController,
) {
    @Provides
    @PerService
    fun syncRepository(d2: D2): SyncRepository = SyncRepositoryImpl(d2)

    @Provides
    @PerService
    internal fun syncPresenter(
        d2: D2,
        preferences: PreferenceProvider,
        syncRepository: SyncRepository,
    ): SyncPresenter =
        SyncPresenterImpl(
            d2,
            preferences,
            syncRepository,
            syncStatusController,
        )
}
