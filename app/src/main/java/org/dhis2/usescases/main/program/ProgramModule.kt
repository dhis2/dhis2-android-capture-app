package org.dhis2.usescases.main.program

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.NotificationsApi
import org.dhis2.data.service.SyncStatusController
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.dhis2.usescases.notifications.presentation.NotificationsView
import org.hisp.dhis.android.core.D2

@Module
class ProgramModule(private val view: ProgramView, private val notificationsView: NotificationsView) {

    @Provides
    @PerFragment
    internal fun programPresenter(
        programRepository: ProgramRepository,
        schedulerProvider: SchedulerProvider,
        themeManager: ThemeManager,
        filterManager: FilterManager,
        matomoAnalyticsController: MatomoAnalyticsController,
        syncStatusController: SyncStatusController
    ): ProgramPresenter {
        return ProgramPresenter(
            view,
            programRepository,
            schedulerProvider,
            themeManager,
            filterManager,
            matomoAnalyticsController,
            syncStatusController
        )
    }

    @Provides
    @PerFragment
    internal fun homeRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        dhisProgramUtils: DhisProgramUtils,
        dhisTrackedEntityInstanceUtils: DhisTrackedEntityInstanceUtils,
        schedulerProvider: SchedulerProvider
    ): ProgramRepository {
        return ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            dhisTrackedEntityInstanceUtils,
            ResourceManager(view.context),
            schedulerProvider
        )
    }

    @Provides
    @PerFragment
    fun provideAnimations(): ProgramAnimation {
        return ProgramAnimation()
    }

    @Provides
    @PerFragment
    internal fun notificationsPresenter(
        getNotifications: GetNotifications,
    ): NotificationsPresenter {
        return NotificationsPresenter(
            notificationsView,
            getNotifications
        )
    }

    @Provides
    @PerFragment
    internal fun getNotifications(
        notificationRepository: NotificationRepository,
    ): GetNotifications {
        return GetNotifications(notificationRepository)
    }

    @Provides
    @PerFragment
    internal fun notificationsRepository(
        d2: D2,
        preferences: PreferenceProvider
    ): NotificationRepository {
        val biometricsConfigApi = d2.retrofit().create(
            NotificationsApi::class.java
        )

        return NotificationD2Repository(
            d2,
            preferences,
            biometricsConfigApi
        )
    }
}
