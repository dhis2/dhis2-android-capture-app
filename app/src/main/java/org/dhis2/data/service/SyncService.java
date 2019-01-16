package org.dhis2.data.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.usescases.main.MainActivity;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class SyncService extends Service implements SyncView {
    private final static int NOTIFICATION_ID = 0xdeadbeef;
    private final static int NOTIFICATION_ID_EVENT = 0xDEADBEEE;
    private final static int NOTIFICATION_ID_TEI = 0xDEADBEED;

    @Inject
    SyncPresenter syncPresenter;

    @Inject
    NotificationManager notificationManager;

    // @NonNull
    SyncResult syncResult;


    @Override
    public void onCreate() {
        super.onCreate();
        // inject dependencies
        ((App) getApplicationContext()).userComponent()
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
    public Consumer<SyncResult> update(SyncState syncState) {
        return result -> {
            Notification notification;
            syncResult = result;
            String channelId = "dhis";
            if (result.inProgress()) {
                notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_black)
                        .setContentTitle(getTextForNotification(syncState))
                        .setContentText(getString(R.string.sync_text))
                        .setProgress(0, 0, true)
                        .setOngoing(true)
                        .build();
            } else if (result.isSuccess()) {
                next(syncState);
                notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_done_black)
                        .setContentTitle(getTextForNotification(syncState) + " " + getString(R.string.sync_complete_title))
                        .setContentText(getString(R.string.sync_complete_text))
                        .build();
            } else if (!result.isSuccess()) { // NOPMD
                next(syncState);
                notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_error_black)
                        .setContentTitle(getTextForNotification(syncState) + " " + getString(R.string.sync_error_title))
                        .setContentText(getString(R.string.sync_error_text))
                        .build();
            } else {
                throw new IllegalStateException();
            }
            notificationManager.notify(getNotId(syncState), notification);
        };
    }

    @NonNull
    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    private void next(SyncState syncState) {
        switch (syncState) {
            case METADATA:
                syncPresenter.syncEvents();
                break;
            case EVENTS:
                syncPresenter.syncTrackedEntities();
                break;
            case TEI:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    public String getTextForNotification(SyncState syncState) {
        switch (syncState) {
            case METADATA:
                return getString(R.string.sync_metadata);
            case EVENTS:
                return getString(R.string.sync_events);
            default:
                return getString(R.string.sync_tei);
        }
    }

    public int getNotId(SyncState syncState) {
        switch (syncState) {
            case METADATA:
                return NOTIFICATION_ID;
            case EVENTS:
                return NOTIFICATION_ID_EVENT;
            case TEI:
                return NOTIFICATION_ID_TEI;
        }
        return -1;
    }
}