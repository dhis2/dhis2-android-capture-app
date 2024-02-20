package org.dhis2.data.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerService;
import org.dhis2.commons.prefs.BasicPreferenceProvider;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.data.notifications.NotificationD2Repository;
import org.dhis2.data.notifications.NotificationsApi;
import org.dhis2.data.notifications.UserGroupsApi;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.usescases.notifications.domain.NotificationRepository;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class ReservedValuesWorkerModule {

    @Provides
    @PerService
    NotificationManager notificationManager(@NonNull Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    @Provides
    @PerService
    SyncRepository syncRepository(@NonNull D2 d2) {

        return new SyncRepositoryImpl(d2);
    }

    @Provides
    @PerService
    NotificationRepository notificationsRepository(@NonNull D2 d2, BasicPreferenceProvider preference) {
        NotificationsApi notificationsApi = d2.retrofit().create(NotificationsApi.class);
        UserGroupsApi userGroupsApi = d2.retrofit().create(UserGroupsApi.class);

        return new NotificationD2Repository(d2, preference, notificationsApi, userGroupsApi);
    }

    @Provides
    @PerService
    SyncPresenter syncPresenter(
            @NonNull D2 d2,
            @NonNull PreferenceProvider preferences,
            @NonNull WorkManagerController workManagerController,
            @NonNull AnalyticsHelper analyticsHelper,
            @NonNull SyncStatusController syncStatusController,
            @NonNull SyncRepository syncRepository,
            @NonNull NotificationRepository notificationsRepository
    ) {
        return new SyncPresenterImpl(d2, preferences, workManagerController, analyticsHelper,
                syncStatusController, syncRepository, notificationsRepository);
    }
}
