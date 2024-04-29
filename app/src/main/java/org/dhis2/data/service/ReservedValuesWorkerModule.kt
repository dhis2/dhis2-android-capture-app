package org.dhis2.data.service

import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerService
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.NotificationsApi
import org.dhis2.data.notifications.UserGroupsApi
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.notifications.domain.NotificationRepository
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
    fun notificationsRepository(
        d2: D2,
        preference: BasicPreferenceProvider
    ): NotificationRepository {
        val notificationsApi = d2.retrofit().create(
            NotificationsApi::class.java
        )
        val userGroupsApi = d2.retrofit().create(UserGroupsApi::class.java)
        return NotificationD2Repository(d2, preference, notificationsApi, userGroupsApi)
    }

    @Provides
    @PerService
    internal fun syncPresenter(
        d2: D2,
        preferences: PreferenceProvider,
        workManagerController: WorkManagerController,
        analyticsHelper: AnalyticsHelper,
        syncStatusController: SyncStatusController,
        syncRepository: SyncRepository,
        notificationsRepository: NotificationRepository
    ): SyncPresenter {
        return SyncPresenterImpl(
            d2,
            preferences,
            workManagerController,
            analyticsHelper,
            syncStatusController,
            syncRepository,
            notificationsRepository,
        )
    }
}
