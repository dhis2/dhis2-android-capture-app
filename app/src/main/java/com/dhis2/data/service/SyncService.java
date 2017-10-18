package com.dhis2.data.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.dhis2.App;
import com.dhis2.R;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class SyncService extends Service implements SyncView {
    private final static int NOTIFICATION_ID = 0xdeadbeef;

    @Inject
    SyncPresenter syncPresenter;

    @Inject
    NotificationManagerCompat notificationManager;

    // @NonNull
    SyncResult syncResult;

    @Override
    public void onCreate() {
        super.onCreate();
//        AndroidInjection.inject(this);
        // inject dependencies
        ((App) getApplicationContext()).getUserComponent()
                .plus(new ServiceModule()).inject(this);
        syncPresenter.onAttach(this);
        syncResult = SyncResult.idle();
    }

    @Override
    public void onDestroy() {
        syncPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (!syncResult.inProgress()) {
            syncPresenter.sync();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new IllegalStateException("This service is not intended for binding.");
    }

    @NonNull
    @Override
    public Consumer<SyncResult> update() {
        return result -> {
            Notification notification;
            syncResult = result;

            if (result.inProgress()) {
                notification = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_sync_black)
                        .setContentTitle(getString(R.string.sync_title))
                        .setContentText(getString(R.string.sync_text))
                        .setProgress(0, 0, true)
                        .setOngoing(true)
                        .build();
            } else if (result.isSuccess()) {
                notification = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_sync_black)
                        .setContentTitle(getString(R.string.sync_complete_title))
                        .setContentText(getString(R.string.sync_complete_text))
                        .build();
            } else if (!result.isSuccess()) { // NOPMD
                notification = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_sync_error_black)
                        .setContentTitle(getString(R.string.sync_error_title))
                        .setContentText(getString(R.string.sync_error_text))
                        .build();
            } else {
                throw new IllegalStateException();
            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        };
    }
}