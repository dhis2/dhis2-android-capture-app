package com.dhis2.data.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.utils.Constants;
import com.dhis2.utils.DateUtils;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class SyncMetadataService extends JobService implements SyncView {
    private final static int NOTIFICATION_ID = 0xdeadbeef;

    @Inject
    SyncPresenter syncPresenter;

    @Inject
    NotificationManager notificationManager;

    // @NonNull
    SyncResult syncResult;
    private JobParameters job;

    @Override
    public void onCreate() {
        super.onCreate();

        if (((App) getApplicationContext()).userComponent() == null)
            stopSelf();
        else
            ((App) getApplicationContext()).userComponent()
                    .plus(new MetadataServiceModule()).inject(this);
    }

    @Override
    public void onDestroy() {
        if (syncPresenter != null)
            syncPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        this.job = job;
        if (syncPresenter != null) {
            syncPresenter.onAttach(this);
            syncResult = SyncResult.idle();
            if (!syncResult.inProgress()) {
                Log.d("SyncMetaDataService", "Metadata job started_"+job.getTag());
                syncPresenter.syncMetaData();
            }
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

    @NonNull
    @Override
    public Consumer<SyncResult> update(SyncState syncState) {
        return result -> {
//            Notification notification;
            syncResult = result;
            String channelId = "dhis";
            String channelName = "Sync";


           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                notificationManager.createNotificationChannel(channel);
            }*/

            if (result.inProgress()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("metaSyncInProgress",true));
               /* notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_black)
                        .setContentTitle(getTextForNotification())
                        .setContentText(getString(R.string.sync_text))
                        .setProgress(0, 0, true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(true)
                        .build();*/
            } else if (result.isSuccess()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("metaSyncInProgress",false));
                SharedPreferences prefs = getSharedPreferences("com.dhis2", Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.LAST_META_SYNC, DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime())).apply();
                prefs.edit().putBoolean(Constants.LAST_META_SYNC_STATUS, true).apply();
                syncPresenter.onDetach();
                jobFinished(job, job.isRecurring());
                /*notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_done_black)
                        .setContentTitle(getTextForNotification() + " " + getString(R.string.sync_complete_title))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentText(getString(R.string.sync_complete_text))
                        .build();*/
            } else if (!result.isSuccess()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("metaSyncInProgress",false));
                SharedPreferences prefs = getSharedPreferences("com.dhis2", Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.LAST_META_SYNC, DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime())).apply();
                prefs.edit().putBoolean(Constants.LAST_META_SYNC_STATUS, false).apply();
                syncPresenter.onDetach();
                /*notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_error_black)
                        .setContentTitle(getTextForNotification() + " " + getString(R.string.sync_error_title))
                        .setContentText(getString(R.string.sync_error_text))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build();*/
            } else {
                throw new IllegalStateException();
            }
//            notificationManager.notify(getNotId(), notification);
        };
    }

    @NonNull
    @Override
    public Context getContext() {
        return getApplicationContext();
    }


    public String getTextForNotification() {
        return getString(R.string.sync_metadata);
    }

    public int getNotId() {
        return NOTIFICATION_ID;
    }
}