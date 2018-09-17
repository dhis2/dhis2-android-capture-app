package org.dhis2.data.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.dhis2.App;
import org.dhis2.R;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

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
                Log.d("SyncMetaDataService", "Metadata job started");
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
            Notification notification;
            syncResult = result;
            String channelId = "dhis";
            String channelName = "Sync";


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                notificationManager.createNotificationChannel(channel);
            }

            if (result.inProgress()) {
                notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_black)
                        .setContentTitle(getTextForNotification())
                        .setContentText(getString(R.string.sync_text))
                        .setProgress(0, 0, true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(true)
                        .build();
            } else if (result.isSuccess()) {
                syncPresenter.onDetach();
                /*if (job.isRecurring())
                    jobFinished(job, true);
                else
                    jobFinished(job, false);*/
                notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_done_black)
                        .setContentTitle(getTextForNotification() + " " + getString(R.string.sync_complete_title))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentText(getString(R.string.sync_complete_text))
                        .build();
            } else if (!result.isSuccess()) {
                syncPresenter.onDetach();
               /* if (job.isRecurring())
                    jobFinished(job, true);
                else
                    jobFinished(job, false);*/
                notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_sync_error_black)
                        .setContentTitle(getTextForNotification() + " " + getString(R.string.sync_error_title))
                        .setContentText(getString(R.string.sync_error_text))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build();
            } else {
                throw new IllegalStateException();
            }
            notificationManager.notify(getNotId(), notification);
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