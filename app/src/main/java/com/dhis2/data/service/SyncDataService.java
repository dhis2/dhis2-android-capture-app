package com.dhis2.data.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dhis2.App;
import com.dhis2.R;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class SyncDataService extends JobService implements SyncView {
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

        Log.d(getClass().getSimpleName(), "DATA SERVICE ONCREATE");

        if (((App) getApplicationContext()).userComponent() == null)
            stopSelf();
        else
            ((App) getApplicationContext()).userComponent()
                    .plus(new DataServiceModule()).inject(this);
    }

    @Override
    public void onDestroy() {
        syncPresenter.onDetach();
        Log.d(getClass().getSimpleName(), "DATA SERVICE ONDESTROY");
        super.onDestroy();
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        syncPresenter.onAttach(this);
        syncResult = SyncResult.idle();
        if (!syncResult.inProgress()) {
            Log.d("SyncDataService", "Job tag " + job.getTag());
            Log.d("SyncDataService", "Job Started");
            syncPresenter.syncEvents();
        }
        return true; //Is there still work going on?
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Should this job be retried?
    }

    @NonNull
    @Override
    public Consumer<SyncResult> update(SyncState syncState) {
        return result -> {
//            Notification notification;
            syncResult = result;
            String channelId = "dhis";
            if (result.inProgress()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress",true));
              /*  notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_black)
                        .setContentTitle(getTextForNotification(syncState))
                        .setContentText(getString(R.string.sync_text))
                        .setProgress(0, 0, true)
                        .setOngoing(true)
                        .build();*/
            } else if (result.isSuccess()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress",false));
                next(syncState);
             /*   notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_done_black)
                        .setContentTitle(getTextForNotification(syncState) + " " + getString(R.string.sync_complete_title))
                        .setContentText(getString(R.string.sync_complete_text))
                        .build();*/
            } else if (!result.isSuccess()) { // NOPMD
                next(syncState);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress",false));
               /* notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_error_black)
                        .setContentTitle(getTextForNotification(syncState) + " " + getString(R.string.sync_error_title))
                        .setContentText(getString(R.string.sync_error_text))
                        .build();*/
            } else {
                throw new IllegalStateException();
            }
//            notificationManager.notify(getNotId(syncState), notification);
        };
    }

    @NonNull
    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    private void next(SyncState syncState) {
        switch (syncState) {
            case EVENTS:
                syncPresenter.syncTrackedEntities();
                break;
            case TEI:
                syncPresenter.onDetach();
                break;
        }
    }

    public String getTextForNotification(SyncState syncState) {
        switch (syncState) {
            case EVENTS:
                return getString(R.string.sync_events);
            default:
                return getString(R.string.sync_tei);
        }
    }

    public int getNotId(SyncState syncState) {
        switch (syncState) {
            case EVENTS:
                return NOTIFICATION_ID_EVENT;
            case TEI:
                return NOTIFICATION_ID_TEI;
        }
        return -1;
    }
}