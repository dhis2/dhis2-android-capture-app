package com.dhis2.data.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import com.dhis2.data.dagger.PerService;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
@PerService
public class DataServiceModule {

    @Provides
    @PerService
    NotificationManagerCompat notificationManager(@NonNull Context context) {
        return NotificationManagerCompat.from(context);
    }

    @Provides
    @PerService
    SyncPresenter syncPresenter(@NonNull D2 d2) {
        return new SyncPresenterImpl(d2);
    }
}
