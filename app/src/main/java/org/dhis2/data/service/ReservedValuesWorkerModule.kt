package org.dhis2.data.service

import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerService
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@Module
class ReservedValuesWorkerModule {
    @Provides
    @PerService
    fun notificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @PerService
    fun syncRepository(d2: D2): SyncRepository {
        return SyncRepositoryImpl(d2)
    }

    @Provides
    @PerService
    internal fun syncPresenter(
        d2: D2,
        preferences: PreferenceProvider,
        workManagerController: WorkManagerController,
        analyticsHelper: AnalyticsHelper,
        syncStatusController: SyncStatusController,
        syncRepository: SyncRepository
    ): SyncPresenter {
        return SyncPresenterImpl(
            d2,
            preferences,
            workManagerController,
            analyticsHelper,
            syncStatusController,
            syncRepository
        )
    }
}
