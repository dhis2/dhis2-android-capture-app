package com.dhis2.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import com.data.dagger.PerService;
import com.data.schedulers.SchedulerProvider;

import org.hisp.dhis.android.core.D2;
import dagger.Module;
import dagger.Provides;

@Module
@PerService
public class ServiceModule {

    @Provides
    @PerService
    NotificationManagerCompat notificationManager(@NonNull Context context) {
        return NotificationManagerCompat.from(context);
    }

    @Provides
    @PerService
    SyncPresenter syncPresenter(@NonNull D2 d2, @NonNull SchedulerProvider schedulerProvider) {
        return new SyncPresenterImpl(d2, schedulerProvider);
    }
}
