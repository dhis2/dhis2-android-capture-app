package org.dhis2.usescases.main.program


import NotificationsApi
import UserGroupsApi
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.UserD2Repository
import org.dhis2.data.service.SyncStatusController
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.UserRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.dhis2.usescases.notifications.presentation.NotificationsView
import org.hisp.dhis.android.core.D2

@Module
class ProgramModule(
    private val view: ProgramView,
    private val notificationsView: NotificationsView
) {
    @Provides
    @PerFragment
    internal fun programViewModelFactory(
        programRepository: ProgramRepository,
        dispatcherProvider: DispatcherProvider,
        featureConfigRepository: FeatureConfigRepository,
        matomoAnalyticsController: MatomoAnalyticsController,
        syncStatusController: SyncStatusController,
    ): ProgramViewModelFactory {
        return ProgramViewModelFactory(
            view,
            programRepository,
            featureConfigRepository,
            dispatcherProvider,
            matomoAnalyticsController,
            syncStatusController,
        )
    }

    @Provides
    @PerFragment
    internal fun homeRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        dhisProgramUtils: DhisProgramUtils,
        schedulerProvider: SchedulerProvider,
        colorUtils: ColorUtils,
        metadataIconProvider: MetadataIconProvider,
    ): ProgramRepository {
        return ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            ResourceManager(view.context, colorUtils),
            metadataIconProvider,
            schedulerProvider,
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
        markNotificationAsRead: MarkNotificationAsRead
    ): NotificationsPresenter {
        return NotificationsPresenter(
            notificationsView,
            getNotifications,
            markNotificationAsRead
        )
    }

    @Provides
    @PerFragment
    internal fun getMarkNotificationAsRead(
        notificationRepository: NotificationRepository,
        userRepository: UserRepository
    ): MarkNotificationAsRead {
        return MarkNotificationAsRead(notificationRepository, userRepository)
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
    fun notificationsRepository(
        d2: D2,
        preference: BasicPreferenceProvider
    ): NotificationRepository {
        val notificationsApi = NotificationsApi(d2.httpServiceClient())

        val userGroupsApi = UserGroupsApi(d2.httpServiceClient())

        return NotificationD2Repository(d2, preference, notificationsApi, userGroupsApi)
    }

    @Provides
    @PerFragment
    internal fun userRepository(
        d2: D2,
    ): UserRepository {
        return UserD2Repository(
            d2
        )
    }
}
